(ns ifgame.room
  "Provides functions for dealing a room"
  (:require [ifgame.object :as object]))

(def ^:private rooms (atom {}))

(defn defroom
  "Add a room to the game.
     id - a keyword for the room.
     name - the name of the room. Display first in room descriptions.
     desc - the full description of the room.
     :north, :south, :east, :west, :up, :down, :northwest, :northeast :southwest - id of rooms in the given direction.
     :props - set of properties for the room.
     :objects - set of ids to objects in the room.
     :fn - the rooms action handler."
  [id name desc & {:keys [north south east west up down northwest northeast southwest southeast props objects fn]
                   :or {north nil
                        south nil
                        east nil
                        west nil
                        up nil
                        down nil
                        northwest nil
                        northeast nil
                        southwest nil
                        southeast nil
                        props #{}
                        objects #{}
                        fn nil}}]
  (swap! rooms assoc id {:name name
                         :desc desc
                         :north north
                         :south south
                         :east east
                         :west west
                         :up up
                         :down down
                         :northwest northwest
                         :northeast northeast
                         :southwest southwest
                         :southeast southeast
                         :props props
                         :objects objects
                         :fn fn}))

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
        objects (:objects room)]
    (when-not (empty? objects)
      (str (reduce (fn [description obj-key]
                     (let [desc (object/describe obj-key :short)]
                       (cond
                         (nil? desc)           description
                         (empty? description)  desc
                         :else                 (str description \newline desc))))
                   ""
                   (:objects room))))))

(defn describe
  "Describe a room and it's contents. Verbosity is :full for the full description, otherwise a short description is
  returned."
  [loc-key verbosity]
  (let [room-desc (if (= verbosity :full)
                    (full-description loc-key)
                    (short-description loc-key))
        object-descriptions (describe-objects loc-key)]
    (if-not (empty? object-descriptions)
      (str room-desc \newline (describe-objects loc-key))
      room-desc)))

(defn visit [loc-key]
  (let [room (get-room loc-key)
        count (:visit-count room 0)]
    (swap! rooms assoc-in [loc-key :visit-count] (inc count))))


(defn objects [loc-key]
  (let [room (get-room loc-key)]
    (:objects room)))

(defn add-object [loc-key object-key]
  (let [new-objects (conj (objects loc-key) object-key)]
    (swap! rooms assoc-in [loc-key :objects] new-objects)))

(defn remove-object [loc-key object-key]
  (let [new-objects (remove #{object-key} (objects loc-key))]
    (swap! rooms assoc-in [loc-key :objects] new-objects)))
