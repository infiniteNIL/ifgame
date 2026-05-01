(ns ifgame.room
  "Provides functions for dealing a room"
  (:require [ifgame.object :as object]))

(def ^:private rooms (atom {}))

(defn room
  "Add a room to the game.
     id - a keyword for the room.
     name - the name of the room. Display first in room descriptions.
     desc - the full description of the room.
     :north, :south, :east, :west, :up, :down, :northwest, :northeast :southwest - id of rooms in the given direction.
     :props - set of properties for the room.
     :contents - set of ids to objects in the room.
     :fn - the rooms action handler."
  [id name desc & {:keys [north south east west up down northwest northeast southwest southeast props contents fn]
                   :or {north     nil
                        south     nil
                        east      nil
                        west      nil
                        up        nil
                        down      nil
                        northwest nil
                        northeast nil
                        southwest nil
                        southeast nil
                        props     #{}
                        contents  #{}
                        fn        nil}}]
  (swap! rooms assoc id {:type      :room
                         :name      name
                         :desc      desc
                         :north     north
                         :south     south
                         :east      east
                         :west      west
                         :up        up
                         :down      down
                         :northwest northwest
                         :northeast northeast
                         :southwest southwest
                         :southeast southeast
                         :props     props
                         :contents  contents
                         :fn        fn}))

#_(defrecord Room [name description
                   north south east west up down
                   northwest northeast southwest southeast
                   has])

(defn get-room [loc-key]
  (loc-key @rooms))

(defn full-description [loc-key]
  (let [room (get-room loc-key)]
    (str (:name room) \newline (:desc room))))

(defn short-description [loc-key]
  (let [room (get-room loc-key)]
    (:name room)))

(defn first-visit? [loc-key]
  (let [room (get-room loc-key)]
    (= 1 (:visit-count room 0))))

(defn describe-objects [loc-key]
  (let [room (get-room loc-key)
        obj-keys (:contents room)]
    (when-not (empty? obj-keys)
      (str (reduce (fn [description obj-key]
                     (let [desc (object/describe obj-key :short)]
                       (cond
                         (nil? desc)           description
                         (empty? description)  desc
                         :else                 (str description \newline desc))))
                   ""
                   obj-keys)))))

(defn describe
  "Describe a room and it's contents. Verbosity is :full for the full description, otherwise a short description is
  returned."
  [loc-key verbosity]
  (let [room-desc (if (= verbosity :full)
                    (full-description loc-key)
                    (short-description loc-key))
        object-descriptions (describe-objects loc-key)]
    (if-not (empty? object-descriptions)
      (str room-desc \newline object-descriptions)
      room-desc)))

(defn visit [loc-key]
  (let [room (get-room loc-key)
        count (:visit-count room 0)]
    (swap! rooms assoc-in [loc-key :visit-count] (inc count))))


(defn contents [loc-key]
  (let [room (get-room loc-key)]
    (:contents room)))

(defn add-object [loc-key object-key]
  (let [new-objects (conj (contents loc-key) object-key)]
    (swap! rooms assoc-in [loc-key :contents] new-objects)))

(defn remove-object [loc-key object-key]
  (let [new-objects (remove #{object-key} (contents loc-key))]
    (swap! rooms assoc-in [loc-key :contents] new-objects)))
