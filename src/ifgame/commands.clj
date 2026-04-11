(ns ifgame.commands
  (:require [ifgame.game :as game]
            [ifgame.object :as object]
            [ifgame.objects :as objects]
            [ifgame.room :as room]))

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

(defn travel [direction game-state]
  (let [destination (get @(game/location game-state) direction)]
    (if (nil? destination)
      (println "You can't go that way.")
      (do
        (room/visit destination)
        (game/set-location game-state destination)))))

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
    (keyword (dirs direction direction))))

(defn- go [ast game-state]
  (let [direction (get-in ast [:direct-object :noun])]
    (travel (normalize-direction direction) game-state)))

(defn- take-object [ast game-state]
  ;; TODO: handle get all
  (let [noun (get-in ast [:direct-object :noun])
        {object-key :key
         object :object} (objects/get-object noun)
        error (get-in ast [:direct-object :error])]
    ;(println "noun:" noun)
    (cond
      (= error :no-known-noun)                      (let [noun (first (get-in ast [:direct-object :words]))]
                                                      (println "You can't take the" (str noun ".")))

      (game/in-inventory? game-state object-key)    (println "You're already carrying that!")

      (object/takeable? object)                     (let [location (game/location game-state)]
                                                      (game/add-to-inventory game-state object-key)
                                                      (room/remove-object location object-key)
                                                      (println "Taken."))

      :else                                         (println "You can't take the" (str noun ".")))))

(defn- drop-object [ast game-state]
  ;; TODO: Handle Drop all
  (let [noun (get-in ast [:direct-object :noun])
        {object-key :key
         object :object} (objects/get-object noun)
        error (get-in ast [:direct-object :error])]
    ;(println "noun:" noun)
    (cond
      (= error :no-known-noun)                          (let [noun (first (get-in ast [:direct-object :words]))]
                                                          (println "You're not carrying a" (str noun ".")))

      (not (game/in-inventory? game-state object-key))  (println "You're not carrying the" (str noun "."))

      :else                                             (let [location (game/location game-state)]
                                                          (game/remove-from-inventory game-state object-key)
                                                          (room/add-object location object-key)
                                                          (println "Dropped.")))))

(defn inventory [game-state]
  (let [object-keys (game/inventory game-state)]
    (if-not (empty? object-keys)
      (do
        (println "You are carrying:")
        (doseq [key object-keys]
          (let [obj (objects/get-object-by-key key)]
            (println "  A" (:short-description obj)))))
      (println "You're not carrying anything."))))

(defn examine [ast game-state]
  ;; TODO: Handle examine all or everything
  (let [noun (get-in ast [:direct-object :noun])
        {object-key :key
         object :object} (objects/get-object noun)
        error (get-in ast [:direct-object :error])]
    (cond
      (= error :no-known-noun)    (let [noun (first (get-in ast [:direct-object :words]))]
                                    (println "You see nothing special about the" (str noun ".")))

      (:full-description object)  (println (:full-description object))

      :else                       (println "You see nothing special about the" (str noun ".")))))

(defn process-cmd [ast game-state]
  ;; TODO: Make verbs definable like rooms and objects instead of hard-coded
  (let [verb (:verb ast)]
    ;(println ast)
    (cond
      (= verb "look")            (println (room/describe (game/location game-state) :full))

      (= verb "quit")            (when (verify-quit)
                                   (swap! game-state game/set-quit))

      (direction? verb)          (travel (normalize-direction verb) game-state)

      (= verb "go")              (go ast game-state)

      (= verb "take")            (take-object ast game-state)

      (= verb "inventory")       (inventory game-state)

      (= verb "drop")            (drop-object ast game-state)

      (= verb "examine")         (examine ast game-state)

      (empty? verb)              (println "Excuse me?")

      :else                      (println "Sorry, I don't know what" (str \" verb \") "means."))))
