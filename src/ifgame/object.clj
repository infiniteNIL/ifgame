(ns ifgame.object)

(def ^:private objects (atom {}))

;; TODO: Do we need an initial description for objects
(defn defobject
  "Add an object to the game.
     id - a keyword for the object.
     :names  - A vector of names that object can be referred to as.
     short-description - A short description for the object. Used to describe in a room or inventory.
     full-description - A full description of the object. Used when examining the object
     :contents - the object keys inside this object (must have container property)
     :props - set of properties for the object.
     :fn - the object's action handler."
  [id & {:keys [names short-description full-description adjectives contents props fn]
         :or {full-description nil
              adjectives []
              contents #{}
              props #{}
              fn nil}}]
  (swap! objects assoc id {:short-description short-description
                           :full-description full-description
                           :names names
                           :adjectives adjectives
                           :contents contents
                           :props props
                           :fn fn}))

(defn get-object [key]
  (get @objects key))

(defn get-key
  "Gets an object by name and returns the key"
  [name]
  (some (fn [key]
          (let [obj (get-object key)]
            (if (some #{name} (:names obj))
              key
              nil)))
        (keys @objects)))

(declare container? scenery?)

(defn describe
  "Returns the description of an object. If verbosity is :full returns a full description, otherwise returns the short
  one."
  [object-key verbosity]
  (let [object (get-object object-key)
        short-desc (:short-description object)
        full-desc (:full-description object)]
    (cond
      (scenery? object)
      nil

      (or (not (container? object-key)) (empty? (:contents object)))
      (if (= verbosity :full)
        (str "There is a " full-desc " here.")
        (str "There is a " short-desc " here."))

      :else
      (str "There is a "
           (if (= verbosity :full) full-desc short-desc)
           " here."
           " The " short-desc " contains:"
           \newline
           (reduce (fn [result obj-key]
                     (let [obj (get-object obj-key)]
                       (str result "  A " (:short-description obj))))
                   ""
                   (:contents object))))))

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

(defn container? [obj-key]
  (let [obj (get-object obj-key)]
    (contains? (:props obj) :container)))

(defn scenery? [obj-or-key]
  (let [obj (if (keyword? obj-or-key)
              (get-object obj-or-key)
              obj-or-key)]
    (contains? (:props obj) :scenery)))

(defn add-contents [container-key obj-key]
  (let [container-obj (get-object container-key)
        old-contents (:contents container-obj)]
    (swap! objects assoc-in [container-key :contents] (conj old-contents obj-key))))
