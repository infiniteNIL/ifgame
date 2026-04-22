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

;; Processing order of objects functions:
;; 1) The actor
;; 2) The vehicle the actor is in
;; 3) The indirect object if any
;; 4) The direct object if any
;; 5) The verb
;; 6) The vehicle again
;; 7) The room the player is in
;; 8) Daemons that have no relation to the player's action
;;
;; If one handles it, the process of command is finished. A function may do something
;; but not handle the command
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
              ;; TODO: Need to pass obj-keys to parser so it can create vocab
              (parser/parse game)
              ;(parser/parse obj-keys)
              (cmd/process-cmd game))
          (game/update-game game))
        (recur current-loc)))))
