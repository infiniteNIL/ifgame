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
(defn -main []
  (game/init)
  (print-title game/state)
  (println)
  (loop [prev-loc nil]
    ;(println @game/state)
    (when-not (game/over? @game/state)
      (let [current-loc (game/location game/state)]
        (when (not= prev-loc current-loc)
          (println (room/describe current-loc (if (room/first-visit? current-loc) :full :short))))
        (-> (get-command)
            (parser/parse game/state)
            (cmd/process-cmd game/state))
        (game/update-state game/state)
        (recur current-loc)))))
