(ns ifgame.commands
  (:require [ifgame.game :as game]
            [ifgame.player :as player]
            [ifgame.room :as room]))

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
      (do
        (room/visit destination)
        (swap! game-state player/set-location destination)))))

(defn normalize-direction [direction]
  (let [dirs {"n" "north"
              "s" "south"
              "e" "east"
              "w" "west"
              "u" "up"
              "d" "down"
              "ne" "northeast"
              "nw" "northwest"
              "se" "southeast"
              "sw" "southwest"}]
    (dirs direction direction)))

(defn process-cmd [ast game-state]
  (let [verb (:verb ast)]
    ;(println ast)
    (cond
      (look-command? verb)
      (println (room/full-description (player/location game-state)))

      (quit-command? verb)
      (when (verify-quit)
        (swap! game-state game/set-quit))

      (direction? verb)
      (travel (normalize-direction verb) game-state)

      (or (= verb "go") (= verb "travel"))
      (let [direction (get-in ast [:direct_object :noun])]
        (travel (normalize-direction direction) game-state))

      :else
      (println "What? I don't know what" (str \" verb \") "means."))))
