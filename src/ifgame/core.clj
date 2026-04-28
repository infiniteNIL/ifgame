(ns ifgame.core
  (:require [ifgame.commands :as cmd]
            [ifgame.room :as room]
            [ifgame.parser :as parser]
            [ifgame.game :as game]
            [ifgame.actions])   ;; needs to be here to load game actions
  (:gen-class))

(defn print-title [game]
  (println (:title @game))
  (println (:headline @game))
  (println "by" (:author @game)))

(defn get-command []
  (println)
  (print "> ")
  (flush)
  (read-line))

;; TODO: Need to handle darkness (when room doesn't have light prop)
(defn start-game [game]
  (print-title game)
  (println)
  (game/init game)
  (loop [prev-loc nil]
    ;(println game)
    (when-not (game/over? game)
      (let [current-loc (:location @game)]
        (when (not= prev-loc current-loc)
          (println (room/describe current-loc (if (room/first-visit? current-loc) :full :short))))
        (let [obj-keys (room/objects current-loc)]
          (-> (get-command)
              (parser/parse game)
              (cmd/process-cmd game))
          (game/update-game game))
        (recur current-loc)))))
