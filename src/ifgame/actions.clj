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

(defn- travel [direction game]
  (let [room-key (:location @game)
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
            (game/set-location game destination-key)))))))

(defn- go [ast game]
  (let [direction (:direction ast) #_(get-in ast [:direct-object :noun])]
    (travel (normalize-direction direction) game)))

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
                     (println "You're not carrying anything."))
                   true)))

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

(defn- take-object [ast game]
  (let [noun (:do-word ast)
        obj-key (:do-key ast)]
    (cond
      (game/in-inventory? game obj-key)             (println "You're already carrying that!")

      (or (= noun "all") (= noun "everything"))     (let [location (:location @game)
                                                          object-keys (room/objects location)]
                                                      (if (not (empty? object-keys))
                                                        (doseq [key object-keys]
                                                          (let [obj (object/get-object key)
                                                                name (first (:names obj))]
                                                            (if (object/takeable? key)
                                                              (do (game/add-to-inventory game key)
                                                                  (room/remove-object location key)
                                                                  (println (str name ":") "Taken."))
                                                              (println (str name ":") "You can't take that."))))
                                                        (println "There is nothing here you can take.")))

      (object/takeable? obj-key)                    (let [location (:location @game)]
                                                      (game/add-to-inventory game obj-key)
                                                      (room/remove-object location obj-key)
                                                      (println "Taken."))

      :else                                         (println "You can't take the" (str noun ".")))))

(defaction :take
           ["take" "get" "pick up"]
           :requires #{}
           :fn (fn [ast game]
                 (let [obj-key (:do-key ast)]
                   (take-object ast game)
                   true)))

