(ns ifgame.actions
  "Defines all the default actions in a typical game"
  (:require [ifgame.action :refer [defaction]]
            [ifgame.object :as object]
            [ifgame.room :as room]
            [ifgame.game :as game]))

(defn- drop-object [ast game]
  (let [noun (:do-word ast)
        obj-key (:do-key ast)]
    (cond
      (or (= noun "all") (= noun "everything"))
      (let [object-keys (:inventory @game)
            location (:location @game)]
        (if object-keys
          (doseq [key object-keys]
            (let [obj (object/get-object key)
                  name (first (:names obj))]
              (game/remove-from-inventory game key)
              (room/add-object location key)
              (println (str name ":") "Dropped.")))
          (println "You're not carrying anything.")))

      (not (game/in-inventory? game obj-key))
      (println "You're not carrying the" (str noun "."))

      :else
      (let [location (:location @game)]
        (game/remove-from-inventory game obj-key)
        (room/add-object location obj-key)
        (println "Dropped.")))))

(defaction :drop
           ["drop" "put down"]
           :requires #{}
           :fn (fn [ast game]
                 (drop-object ast game)
                 true))

(defn- examine [ast _game]
  (let [noun (:do-word ast)
        object (:direct-object ast)]
    (cond
      (or (= noun "all") (= noun "everything"))
      (println "You can only examine things one at a time.")

      (:full-description object)
      (println (:full-description object))

      :else
      (println "You see nothing special about the" (str noun ".")))))

(defaction :examine
           ["x" "examine"]
           :requires #{}
           :fn (fn [ast game]
                 (examine ast game)
                 true))

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
  (let [direction (:direction ast)]
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
           :fn (fn [_ast game]
                 (let [object-keys (:inventory @game)]
                   (if-not (empty? object-keys)
                     (do
                       (println "You are carrying:")
                       (doseq [key object-keys]
                         (let [obj (object/get-object key)]
                           ;; TODO: Needs to be recursive to handle containers within containers
                           (println "  A" (:short-description obj))
                           (when (and (object/container? key) (not-empty (:contents obj)))
                             (println "  The" (:short-description obj) "contains:")
                             (doseq [key (:contents obj)]
                               (let [o (object/get-object key)]
                                 (println "    A" (:short-description o))))))))
                     (println "You're not carrying anything."))
                   true)))

(defaction :look
           ["l" "look"]
           :requires #{}
           :fn (fn [_ast game]
                 (println (room/describe (:location @game) :full))
                 true))

(declare take-object)

;; TODO: put bird in nest and put nest on branch are different actions (different prepositions)
(defaction :place
           ["place" "put"]
           ;; TODO: Parser should enforce this
           :requires #{:direct-object :preposition :indirect-object}
           :fn (fn [ast game]
                 ;(println "place ast:" ast)
                 (let [do-key (:do-key ast)
                       do-obj (object/get-object do-key)
                       io-key (:io-key ast)
                       io-obj (object/get-object io-key)]
                   (when (not (game/in-inventory? game do-key))
                     (take-object ast game))
                   (cond
                     (not (game/in-inventory? game do-key))
                     (do (println "You're not carrying the" (:short-description do-obj))
                         true)

                     (not (object/container? io-key))
                     (do (println "You can't put anything in the" (:short-description io-obj))
                         true)

                     :else
                     (do (object/add-contents io-key do-key)
                         (game/remove-from-inventory game do-key)
                         (println "You place the" (:short-description do-obj) "in the" (str (:short-description io-obj) "."))
                         true)))))

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
           :fn (fn [_ast game]
                 (when (verify-quit)
                   (game/set-quit game))
                 true))

(defn- take-object [ast game]
  (let [noun (:do-word ast)
        obj-key (:do-key ast)]
    ;; TODO: Should be able to get objects in containers
    (cond
      (game/in-inventory? game obj-key)
      (println "You're already carrying that!")

      (or (= noun "all") (= noun "everything"))
      (let [location (:location @game)
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

      (object/takeable? obj-key)
      (let [location (:location @game)]
        (game/add-to-inventory game obj-key)
        (room/remove-object location obj-key)
        (println "Taken."))

      :else
      (println "You can't take the" (str noun ".")))))

(defaction :take
           ["take" "get" "pick up"]
           :requires #{}
           :fn (fn [ast game]
                 (take-object ast game)
                 true))

