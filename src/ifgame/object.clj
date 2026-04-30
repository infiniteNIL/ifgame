(ns ifgame.object)

(def ^:private objects (atom {}))

(defn object
  "Add an object to the game.
     id - a keyword for the object.
     name - Name or phrase for the object. Used to plug into sentences.
     synonyms  - A vector of names that object can be referred to as. (used by parser)
     :desc - A short description for the object. Used to describe in a room or inventory.
     :first-desc - A description for the object when it is first seen.
     :contents - the object keys inside this object (must have container property)
     :props - set of properties for the object.
     :fn - the object's action handler."
  [id name synonyms & {:keys [desc first-desc adjectives contents props fn]
                       :or {desc (str "There is a " name " here.")
                            first-desc nil
                            adjectives []
                            contents #{}
                            props #{}
                            fn nil}}]
  (swap! objects assoc id {:name name
                           :synonyms synonyms
                           :first-desc first-desc
                           :desc desc
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
            (if (some #{name} (:synonyms obj))
              key
              nil)))
        (keys @objects)))

(declare container? scenery?)

(defn describe
  "Returns the description of an object. If verbosity is :full returns a full description, otherwise returns the short
  one."
  [object-key verbosity]
  (let [object (get-object object-key)
        short-desc (:desc object)
        full-desc (:first-desc object)]
    (cond
      (scenery? object)
      nil

      (or (not (container? object-key)) (empty? (:contents object)))
      (if (= verbosity :full)
        (:first-desc object)
        (:desc object))

      :else
      (str (if (= verbosity :full) full-desc short-desc)
           " The " (:name object) " contains:"
           \newline
           (reduce (fn [result obj-key]
                     (let [obj (get-object obj-key)]
                       (str result "  A " (:name obj))))
                   ""
                   (:contents object))))))

(defn get-names [object-key]
  (let [obj (get-object object-key)]
    (:synonyms obj)))

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
