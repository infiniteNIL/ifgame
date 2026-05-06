(ns ifgame.room
  "Provides functions for dealing with a room"
  (:require [clojure.string :as str]
            [ifgame.object :as object]))

(def ^:private rooms (atom {}))

(defn room
  "Add a room to the game.
     id - a keyword for the room.
     name - the name of the room. Display first in room descriptions.
     desc - the full description of the room.
     :north, :south, :east, :west, :up, :down, :northwest, :northeast :southwest - id of rooms in the given direction.
     :props - set of properties for the room.
     :contents - set of ids to objects in the room.
     :before-handler - the rooms before action handler. Receives ast and game. Called before any object handlers."
  [id name desc & {:keys [north south east west up down northwest northeast southwest southeast props contents before-handler after-handler]
                   :or {north          nil
                        south          nil
                        east           nil
                        west           nil
                        up             nil
                        down           nil
                        northwest      nil
                        northeast      nil
                        southwest      nil
                        southeast      nil
                        props          #{}
                        contents       #{}
                        before-handler nil
                        after-handler  nil}}]
  (swap! rooms assoc id {:type           :room
                         :name           name
                         :desc           desc
                         :north          north
                         :south          south
                         :east           east
                         :west           west
                         :up             up
                         :down           down
                         :northwest      northwest
                         :northeast      northeast
                         :southwest      southwest
                         :southeast      southeast
                         :props          props
                         :contents       contents
                         :before-handler before-handler
                         :after-handler  after-handler}))

#_(defrecord Room [name description
                   north south east west up down
                   northwest northeast southwest southeast
                   has])

(defn get-room
  "Get a room given its key"
  [loc-key]
  (loc-key @rooms))

(defn full-description
  "Get the full description of a room given its key"
  [loc-key]
  (let [room (get-room loc-key)]
    (str (:name room) \newline (:desc room))))

(defn short-description
  "Get the short description of a room given its key."
  [loc-key]
  (let [room (get-room loc-key)]
    (:name room)))

(defn first-visit?
  "Given a room key, returns if this is the first time the player has been in the room."
  [loc-key]
  (let [room (get-room loc-key)]
    (= 1 (:visit-count room 0))))

(defn describe-objects
  "Returns a string describing all the objects in a room, given its key."
  [loc-key]
  (let [room (get-room loc-key)
        obj-keys (:contents room)]
    (when-not (empty? obj-keys)
      (str (reduce (fn [description obj-key]
                     (let [desc (object/describe obj-key :short)]
                       (cond
                         (str/blank? desc)        description
                         (str/blank? description) desc
                         :else                    (str description \newline desc))))
                   ""
                   obj-keys)))))

(defn describe
  "Describe a room and its contents, given its key. Verbosity is :full for the full description, otherwise a short description is
  returned."
  [loc-key verbosity]
  ;; TODO: Need to handle darkness (when room doesn't have light prop)
  (let [room-desc (if (= verbosity :full)
                    (full-description loc-key)
                    (short-description loc-key))
        object-descriptions (describe-objects loc-key)]
    (if-not (empty? object-descriptions)
      (str room-desc \newline object-descriptions)
      room-desc)))

(defn visit
  "Record that a room has been visited by the player, given its key."
  [loc-key]
  (let [room (get-room loc-key)
        count (:visit-count room 0)]
    (swap! rooms assoc-in [loc-key :visit-count] (inc count))))


(defn contents
  "Return the contents of a room, given its key."
  [loc-key]
  (let [room (get-room loc-key)]
    (:contents room)))

(defn complete-contents
  "Get all the object keys of objects in the room, the room's containers, and the room's supporters."
  [loc-key]
  (let [room (get-room loc-key)
        room-contents (:contents room)]
    (concat room-contents
            (mapcat (fn [obj-key]
                      (let [obj (object/get-object obj-key)]
                        (:contents obj)))
                    room-contents)
            (mapcat (fn [obj-key]
                      (let [obj (object/get-object obj-key)]
                        (:supports obj)))
                    room-contents))))

(defn add-object
  "Add an object to a room, given the room's key and the object's key."
  [loc-key object-key]
  (let [new-objects (conj (contents loc-key) object-key)]
    (swap! rooms assoc-in [loc-key :contents] new-objects)))

(defn remove-object
  "Remove an object from a room, given the room's key and the object's key.
   Also removes the object if it is in a container or supporter in the room."
  [loc-key object-key]
  (let [objects (contents loc-key)]
    (if (contains? objects object-key)
      ;; Simple case. object at top level
      (swap! rooms assoc-in [loc-key :contents] (disj objects object-key))

      ;; Complex case. object in container or supporter
      ;; First we need to find container or supporter of object-key
      (when-let [parent-key (object/find-parent object-key objects)]
        (object/remove-child parent-key object-key)))))
