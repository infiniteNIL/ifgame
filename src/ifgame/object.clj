(ns ifgame.object)

(def ^:private objects (atom {}))

(defn defobject
  "Add an object to the game.
     id - a keyword for the object.
     :names  - A vector of names that object can be referred to as.
     short-description - A short description for the object. Used to describe in a room or inventory.
     full-description - A full description of the object. Used when examining the object
     :props - set of properties for the object.
     :fn - the object's action handler."
  [id & {:keys [names short-description full-description adjectives props fn]
         :or {full-description nil
              adjectives []
              props #{}
              fn nil}}]
  (swap! objects assoc id {:short-description short-description
                           :full-description full-description
                           :names names
                           :adjectives adjectives
                           :props props
                           :fn fn}))

(defn get-object [key]
  (get @objects key))

(defn get-key
  "Get's an object by name and returns the key"
  [name]
  (some (fn [key]
          (let [obj (get-object key)]
            (if (some #{name} (:names obj))
              key
              nil)))
        (keys @objects)))

(defn describe
  "Returns the description of an object. If verbosity is :full returns a full description, otherwise returns the short
  one."
  [object-key verbosity]
  (let [object (get-object object-key)]
    (cond
      (contains? (:props object) :scenery)   nil
      (= verbosity :full)                    (str "There is a " (:full-description object) " here.")
      :else                                  (str "There is a " (:short-description object) " here."))))

(defn get-names [object-key]
  (let [obj (get-object object-key)]
    (:names obj)))

(defn get-adjectives [object-key]
  (let [obj (get-object object-key)]
    (:adjectives obj)))

(defn takeable? [obj-key]
  (let [obj (get-object obj-key)]
    (not (or (contains? (:props obj) :scenery)
             (contains? (:props obj) :static)))))
