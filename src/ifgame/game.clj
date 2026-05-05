(ns ifgame.game
  (:require [ifgame.object :as object]
            [ifgame.room :as room]))

;(defrecord Game [title headline author state turns location health])

(defn create [title headline author start-location]
  (atom {:title title
         :headline headline
         :author author
         :state :starting
         :turns 0
         :location start-location
         :inventory #{}
         :health 100}))

(defn set-location [game new-location]
  (swap! game assoc :location new-location))

(defn init [game]
  (room/visit (:location @game)))

(defn quit [game]
  (swap! game assoc :state :quit))

(defn won [game]
  (swap! game assoc :state :won))

(defn update-game [game]
  ; Update turns and state
  (let [new-turns (inc (:turns @game))
        new-state (if (= (:state @game) :starting)
                    :in-progress
                    (:state @game))]
    (swap! game assoc :turns new-turns :state new-state)))

(defn over? [game]
  (or (= (:state @game) :quit)
      (= (:state @game) :won)))

(defn won? [game]
  (= (:state @game) :won))

(defn add-to-inventory [game object-key]
  (let [new-inventory (conj (:inventory @game) object-key)]
    (swap! game assoc :inventory new-inventory)))

(defn remove-from-inventory [game object-key]
  (let [new-inventory (disj (:inventory @game) object-key)]
    (swap! game assoc :inventory new-inventory)))

(defn complete-inventory
  "Get all the keys of objects in the player's inventory, and those object's containees, and supporters."
  [game]
  (let [obj-keys (:inventory @game)]
    (set (concat obj-keys
                 (mapcat (fn [obj-key]
                           (let [obj (object/get-object obj-key)]
                             (:contents obj)))
                         obj-keys)
                 (mapcat (fn [obj-key]
                           (let [obj (object/get-object obj-key)]
                             (:supports obj)))
                         obj-keys)))))

(defn in-inventory? [game object-key]
  (contains? (complete-inventory game) object-key))

(defn move-player [game dest-key]
  (set-location game dest-key))

(defn move-object
  "Move an object from one room to another.
    obj-key is the key of the object to move.
    src-room-key is the key of the room to move object from.
    dst-room-key is the key of the room to move the object to."
  [obj-key src-room-key dest-room-key]
  (let [src-room (room/get-room src-room-key)]
    (assert (contains? (:contents src-room) obj-key) "The object needs to be in the src-room.")
    (room/remove-object src-room-key obj-key)
    (room/add-object dest-room-key obj-key)))

