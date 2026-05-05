(ns ifgame.actions
  "Defines all the default actions in a typical game"
  (:require [ifgame.action :refer [action]]
            [ifgame.object :as object]
            [ifgame.room :as room]
            [ifgame.game :as game]))

(action :climb
        ["climb" "climb up"]
        :requires #{}
        :fn (fn [_ast _game]
              (println "You can't climb that.")
              true))

(defn- drop-object [ast game]
  (let [noun (:do-word ast)
        obj-key (:do-key ast)]
    (cond
      (or (= noun "all") (= noun "everything"))
      (let [object-keys (:inventory @game)
            location (:location @game)]
        (if (seq object-keys)
          (doseq [key object-keys]
            (let [obj (object/get-object key)]
              ;; TODO: Handle object in container or supporter in inventory
              (game/remove-from-inventory game key)
              (room/add-object location key)
              (println (str (:name obj) ":") "Dropped.")))
          (println "You're not carrying anything.")))

      (not (game/in-inventory? game obj-key))
      (println "You're not carrying the" (str noun "."))

      :else
      (let [location (:location @game)]
        ;; TODO: Handle object in container or supporter in inventory
        (game/remove-from-inventory game obj-key)
        (room/add-object location obj-key)
        (println "Dropped.")))))

(action :drop
        ["drop" "put down"]
        :requires #{}
        :fn (fn [ast game]
              (drop-object ast game)
              true))

(action :enter
        ["enter" "in" "go in"]
        :requires #{}
        :fn (fn [_ast _game]
              (println "That's not something you can enter.")
              true))

(defn- examine [ast _game]
  (let [noun (:do-word ast)
        object (:direct-object ast)]
    ;; For special objects, the objects handler needs to handle this action
    (if (or (= noun "all") (= noun "everything"))
      (println "You can only examine things one at a time.")
      (println "You see nothing special about the" (str (:name object) ".")))))

(action :examine
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

(action :go
        ["go" "walk"]
        :requires #{}
        :fn (fn [ast game-state]
              (go ast game-state)
              true))

(action :inventory
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
                        (println "  A" (:name obj))
                        (when (and (object/supporter? key) (not-empty (:supports obj)))
                          (println "  Sitting on the" (:name obj) "is:")
                          (doseq [key (:supports obj)]
                            (let [o (object/get-object key)]
                              (println "    A" (:name o)))))
                        (when (and (object/container? key) (not-empty (:contents obj)))
                          (println "  The" (:name obj) "contains:")
                          (doseq [key (:contents obj)]
                            (let [o (object/get-object key)]
                              (println "    A" (:name o))))))))
                  (println "You're not carrying anything."))
                true)))

;; TODO: Need to handle "listen to"
(action :listen
        ["listen" "listen to"]
        :requires #{}
        :fn (fn [_ast _game]
              (println "You hear nothing unexpected.")
              true))
(action :look
        ["l" "look"]
        :requires #{}
        :fn (fn [_ast game]
              (println (room/describe (:location @game) :full))
              true))

(declare take-object)

(action :place
        ["place" "put"]
        ;; TODO: Parser should enforce this
        :requires #{:direct-object :preposition :indirect-object}
        :fn (fn [ast game]
              (let [do-key (:do-key ast)
                    do-obj (object/get-object do-key)
                    prep (:preposition ast)
                    io-key (:io-key ast)
                    io-obj (object/get-object io-key)]
                (when (not (game/in-inventory? game do-key))
                  (take-object ast game))
                (cond
                  (not (game/in-inventory? game do-key))
                  (do (println "You're not carrying the" (:name do-obj))
                      true)

                  (= prep "in")
                  (if (object/container? io-key)
                    (do (object/add-contents io-key do-key)
                        (game/remove-from-inventory game do-key)
                        (println "You place the" (:name do-obj) "in the" (str (:name io-obj) "."))
                        true)
                    (do (println "You can't put anything in the" (:name io-obj))
                        true))

                  (= prep "on")
                  (if (object/supporter? io-key)
                    (do (object/add-support io-key do-key)
                        (game/remove-from-inventory game do-key)
                        (println "You place the" (:name do-obj) "on the" (str (:name io-obj) "."))
                        true)
                    (do (println "You can't put anything on the" (:name io-obj))
                        true))

                  :else
                  false))))

(defn- verify-quit []
  (print "Are you sure? (Y/n) ")
  (flush)
  (let [answer (read-line)]
    (or (= answer "y")
        (= answer "Y")
        (= answer ""))))

(action :quit
        ["q" "quit"]
        :requires #{}
        :fn (fn [_ast game]
              (when (verify-quit)
                (game/quit game))
              true))

(defn- take-object [ast game]
  (let [noun (:do-word ast)
        obj-key (:do-key ast)]
    ;; TODO: Should be able to get objects in containers
    ;; TODO: Should be able to get objects on supporters
    (cond
      (game/in-inventory? game obj-key)
      (println "You're already carrying that!")

      (or (= noun "all") (= noun "everything"))
      (let [location (:location @game)
            object-keys (room/contents location)]
        (if (not (empty? object-keys))
          (doseq [key object-keys]
            (let [obj (object/get-object key)
                  name (:name obj)]
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

(action :take
        ["take" "get" "pick up"]
        :requires #{}
        :fn (fn [ast game]
              (take-object ast game)
              true))

