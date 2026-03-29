(ns ifgame.core
  (:require [ifgame.commands :as cmd]
            [ifgame.room :as room]
            [ifgame.parser :as parser]
            [ifgame.player :as player]
            [ifgame.game :as game])
  (:gen-class))


(defn get-command []
  (println)
  (print "> ")
  (flush)
  (read-line))

(defn print-title [game-state]
  (println (game/title game-state))
  (println "by" (:author game-state)))

(defn -main []
  (print-title @game/state)
  (println)
  (loop [prev-location nil]
    (when (not (game/over? @game/state))
      (room/describe-location (player/location game/state))
      (let [cmd (get-command)]
        (cmd/process-cmd (parser/parse cmd) game/state)
        (recur (player/location game/state))))))
