(ns ifgame.core
  (:require [ifgame.commands :as cmd]
            [ifgame.room :as room]
            [ifgame.parser :as parser]
            [ifgame.game :as game])
  (:gen-class))

(defn print-title [game-state]
  (println (game/title @game-state))
  (println (game/headline @game-state))
  (println "by" (:author @game-state)))

(defn get-command []
  (println)
  (print "> ")
  (flush)
  (read-line))

(defn -main []
  (game/init)
  (print-title game/state)
  (println)
  (loop [prev-loc nil]
    ;(println @game/state)
    (when-not (game/over? @game/state)
      (let [current-loc (game/location game/state)]
        ;(println "room objects:" (room/objects current-loc))
        (when (not= prev-loc current-loc)
          (println (room/describe current-loc (if (room/first-visit? current-loc) :full :short))))
        (-> (get-command)
            (parser/parse game/state)
            (cmd/process-cmd game/state))
        (game/update-state game/state)
        (recur current-loc)))))
