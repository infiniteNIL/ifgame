(ns ifgame.commands
  (:require [ifgame.game :as game]
            [ifgame.object :as object]
            [ifgame.room :as room]
            [ifgame.action :as action]))

(defn- verify-quit []
  (print "Are you sure? (Y/n) ")
  (flush)
  (let [answer (read-line)]
    (or (= answer "y")
        (= answer "Y")
        (= answer ""))))

(defn- direction? [verb]
  (contains? #{"n" "north" "s" "south" "e" "east" "w" "west"
               "u" "up" "d" "down"
               "nw" "northwest" "ne" "northeast"
               "sw" "southwest" "se" "southeast"}
             verb))

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

(defn- go [ast game-state]
  (let [direction (get-in ast [:direct-object :noun])]
    (travel (normalize-direction direction) game-state)))

(defn- take-object [ast game]
  (let [noun (get-in ast [:direct-object :noun])
        obj-key (object/get-key noun)
        error (get-in ast [:direct-object :error])]
    (cond
      (= error :no-known-noun)                      (let [noun (first (get-in ast [:direct-object :words]))]
                                                      (println "You can't take the" (str noun ".")))

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

(defn- drop-object [ast game]
  (let [noun (get-in ast [:direct-object :noun])
        obj-key (object/get-key noun)
        error (get-in ast [:direct-object :error])]
    ;(println "noun:" noun)
    (cond
      (= error :no-known-noun)                    (let [noun (first (get-in ast [:direct-object :words]))]
                                                    (println "You're not carrying a" (str noun ".")))

      (or (= noun "all") (= noun "everything"))   (let [object-keys (:inventory @game)
                                                        location (:location @game)]
                                                    (if object-keys
                                                      (doseq [key object-keys]
                                                        (let [obj (object/get-object key)
                                                              name (first (:names obj))]
                                                          (game/remove-from-inventory game key)
                                                          (room/add-object location key)
                                                          (println (str name ":") "Dropped.")))
                                                      (println "You're not carrying anything.")))

      (not (game/in-inventory? game obj-key))     (println "You're not carrying the" (str noun "."))

      :else                                       (let [location (:location @game)]
                                                    (game/remove-from-inventory game obj-key)
                                                    (room/add-object location obj-key)
                                                    (println "Dropped.")))))

(defn- inventory [game]
  (let [object-keys (:inventory @game)]
    (if-not (empty? object-keys)
      (do
        (println "You are carrying:")
        (doseq [key object-keys]
          (let [obj (object/get-object key)]
            (println "  A" (:short-description obj)))))
      (println "You're not carrying anything."))))

(defn- examine [ast]
  (let [noun (get-in ast [:direct-object :noun])
        obj-key (object/get-key noun)
        object (object/get-object obj-key)
        error (get-in ast [:direct-object :error])]
    (cond
      (= error :no-known-noun)    (let [noun (first (get-in ast [:direct-object :words]))]
                                    (println "You see nothing special about the" (str noun ".")))

      (or (= noun "all")
          (= noun "everything"))  (println "You can only examine things one at a time.")

      (:full-description object)  (println (:full-description object))

      :else                       (println "You see nothing special about the" (str noun ".")))))

(defn process-cmd [ast game]
  ;; TODO: Make verbs definable like rooms and objects instead of hard-coded
  (let [verb (:verb ast)
        ;; TODO: need transform, so s -> go south, for example
        ;; TODO: Should just pass direct object and indirect object to action
        ;; TODO: Parser should resolve action, direct object and indirect object
        action (action/get-action verb)
        action-fn (:fn action)]
    ;(println ast)
    ;(println "action:" action)
    (cond
      (and action-fn
           (action-fn ast game)) true

      (= verb "look")            (println (room/describe (:location @game) :full))

      (= verb "quit")            (when (verify-quit)
                                   (game/set-quit game))

      (direction? verb)          (travel (normalize-direction verb) game)

      (= verb "go")              (go ast game)

      (= verb "take")            (take-object ast game)

      (= verb "inventory")       (inventory game)

      (= verb "drop")            (drop-object ast game)

      (= verb "examine")         (examine ast)

      (empty? verb)              (println "Excuse me?")

      :else                      (println "Sorry, I don't know what" (str \" verb \") "means."))))
