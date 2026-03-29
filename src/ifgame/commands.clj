(ns ifgame.commands
  (:require [ifgame.game :as game]
            [ifgame.player :as player]
            [ifgame.room :as room]))

(defn verb [ast]
  (get (first ast) 1))

(defn quit-command? [verb]
  (some #{verb} '("quit" "q" "x" "exit")))

(defn verify-quit []
  (print "Are you sure? (Y/n) ")
  (flush)
  (let [answer (read-line)]
    (or (= answer "y")
        (= answer "Y")
        (= answer ""))))

(defn direction? [verb]
  (some #{verb} '("n" "north" "s" "south" "e" "east" "w" "west"
                  "u" "up" "d" "down"
                  "nw" "northwest" "ne" "northeast"
                  "sw" "southwest" "se" "southeast")))

(defn look-command? [verb]
  (or (= verb "l") (= verb "look")))

(defn travel [direction game-state]
  (let [destination (get-in @(player/location game-state) [:exits direction])]
    (if (nil? destination)
      (println "You can't go that way.")
      (swap! game-state player/set-location destination))))

(defn process-cmd [ast game-state]
  (println ast)
  (let [verb (verb ast)]
    (cond
      (look-command? verb) (room/describe-location (player/location game-state))
      (quit-command? verb) (if (verify-quit)
                             (swap! game-state game/set-quit))
      (direction? verb) (travel verb game-state)
      :else (println "What? I don't know what" (str \" verb \") "means."))))
