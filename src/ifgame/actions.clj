(ns ifgame.actions
  "Defines all the default actions in a typical game"
  (:require [ifgame.action :refer [defaction]]
            [ifgame.object :as object]
            [ifgame.room :as room]
            [ifgame.game :as game]))

(defaction :drop
           ["drop" "put down"]
           :requires #{}
           :fn (fn [ast game-state]))

(defaction :examine
           ["x" "examine"]
           :requires #{}
           :fn (fn [ast game-state]))

(defn- normalize-direction [direction]
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

(defn- travel [direction game-state]
  (let [room-key (:location game-state)
        room (room/get-room room-key)
        destination-key (get room direction)]
    ;(println "travel destination-key:" destination-key)
    ;(println "travel destination:" destination)
    (if (nil? destination-key)
      (println "You can't go that way.")
      (let [destination (room/get-room destination-key)]
        (if (nil? destination)
          (println "Error - Invalid room id:" destination-key)
          (do
            (room/visit destination-key)
            (println "setting location in travel")
            (game/set-location game-state destination-key)))))))

(defn- go [ast game-state]
  (let [direction (get-in ast [:direct-object :noun])]
    (travel (normalize-direction direction) game-state)))

(defaction :go
           ["go" "walk"]
           :requires #{}
           :fn (fn [ast game-state]
                 (go ast game-state)
                 true))

(defaction :inventory
           ["i" "inventory"]
           :requires #{}
           :fn (fn [ast game]
                 (let [object-keys (:inventory @game)]
                   (if-not (empty? object-keys)
                     (do
                       (println "You are carrying:")
                       (doseq [key object-keys]
                         (let [obj (object/get-object key)]
                           (println "  A" (:short-description obj)))))
                     (println "You're not carrying anything.")))))

(defaction :look
           ["l" "look"]
           :requires #{}
           :fn (fn [ast game]
                 (println (room/describe (:location @game) :full))
                 true))

(defn- verify-quit []
  (print "Are you sure? (Y/n) ")
  (flush)
  (let [answer (read-line)]
    (or (= answer "y")
        (= answer "Y")
        (= answer ""))))

(defaction :quit
           ["q" "quit"]
           :requires #{}
           :fn (fn [ast game]
                 (when (verify-quit)
                   (game/set-quit game))
                 true))

(defaction :take
           ["take" "get" "pick up"]
           :requires #{}
           :fn (fn [ast game-state]))

