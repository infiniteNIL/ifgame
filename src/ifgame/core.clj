(ns ifgame.core
  (:require [ifgame.room :as room]
            [ifgame.player :as player]
            [ifgame.game :as game])
  (:gen-class))


(defn get-command []
  (println)
  (print "> ")
  (flush)
  (read-line))

(defn quit-command? [cmd]
  (some #{cmd} '("quit" "q" "x" "exit")))

(defn verify-quit []
  (print "Are you sure? (Y/n) ")
  (flush)
  (let [answer (read-line)]
    (or (= answer "y")
        (= answer "Y")
        (= answer ""))))

(defn go [direction-str game-state]
  (let [destination (get-in @(player/location game-state) [:exits direction-str])]
    (if (nil? destination)
      (println "You can't go that way.")
      (swap! game-state player/set-location destination))))

(defn process-command [cmd game-state]
  (cond
    (quit-command? cmd)
    (if (verify-quit)
      (swap! game-state game/set-quit))

    (or (= cmd "n") (= cmd "north"))
    (go "north" game-state)

    (or (= cmd "s") (= cmd "south"))
    (go "south" game-state)

    :else
    (println "I don't know how to" cmd)))

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
        (process-command cmd game/state)
        (recur (player/location game/state))))))
