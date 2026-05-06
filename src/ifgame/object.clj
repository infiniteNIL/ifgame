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
  [id name synonyms & {:keys [desc first-desc adjectives contents supports props fn]
                       :or {desc (str "There is a " name " here.")
                            first-desc nil
                            adjectives []
                            contents #{}
                            supports #{}
                            props #{}
                            fn nil}}]
  (swap! objects assoc id {:type :object
                           :name name
                           :synonyms synonyms
                           :first-desc first-desc
                           :desc desc
                           :adjectives adjectives
                           :contents contents
                           :supports supports
                           :props props
                           :fn fn}))

(defn get-object
  "Get an object by its key."
  [key]
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

(declare container? scenery? supporter?)

(defn- describe-contents
  "Given an object key, returns a string describing the contents of the object.
  If the object is not a container or is empty, returns nil."
  [obj-key]
  (let [obj (get-object obj-key)]
    (if (and (container? obj-key) (not-empty (:contents obj)))
      (str \newline
           "The " (:name obj) " contains:" \newline
           (reduce (fn [result obj-key]
                     (let [obj (get-object obj-key)]
                       (str result "  A " (:name obj))))
                   ""
                   (:contents obj)))
      nil)))

(defn- describe-supports
  "Given an object key, returns a string describing the supports of the object.
  If the object is not a supporter or is empty, returns nil."
  [obj-key]
  (let [obj (get-object obj-key)]
    (if (and (supporter? obj-key) (not-empty (:supports obj)))
      (str \newline
           "On top of the " (:name obj) " is:" \newline
           (reduce (fn [result obj-key]
                     (let [obj (get-object obj-key)]
                       (str result "  A " (:name obj))))
                   ""
                   (:supports obj)))
      nil)))

(defn describe
  "Returns the description of an object given its key. If verbosity is :full returns a full description, otherwise returns the short
  description."
  [object-key verbosity]
  (let [object (get-object object-key)
        short-desc (:desc object)
        full-desc (:first-desc object)
        first-line (if (= verbosity :full) full-desc short-desc)
        contents-desc (describe-contents object-key)
        supports-desc (describe-supports object-key)]
    ;(println "object-key:" object-key)
    ;(println "first-line:" first-line)
    ;(println "contents-desc:" contents-desc)
    ;(println "supports-desc:" supports-desc)
    (if (scenery? object)
      ;; Scenery objects don't need initial desc
      (str contents-desc supports-desc)
      (str first-line contents-desc supports-desc))))

(defn get-names
  "Get the names an object goes by given its key."
  [object-key]
  (let [obj (get-object object-key)]
    (:synonyms obj)))

(defn get-adjectives
  "Get the adjectives for an object given its key."
  [object-key]
  (let [obj (get-object object-key)]
    (:adjectives obj)))

(defn- has-prop?
  "Returns whether an object has a property or not, given the object or its key, and the property."
  [obj-or-key property]
  (let [obj (if (keyword? obj-or-key)
              (get-object obj-or-key)
              obj-or-key)]
    (contains? (:props obj) property)))

(defn takeable?
  "Returns whether an object is takeable given its key."
  [obj-key]
  (not (or (has-prop? obj-key :scenery)
           (has-prop? obj-key :supporter))))

(defn container?
  "Returns whether an object is a container given its key."
  [obj-key]
  (has-prop? obj-key :container))

(defn supporter?
  "Returns whether an object is a supporter given its key."
  [obj-key]
  (has-prop? obj-key :supporter))

(defn scenery?
  "Returns whether an object is scenery or not. You can pass in the object itself or its key."
  [obj-or-key]
  (has-prop? obj-or-key :scenery))

(defn add-contents
  "Adds an object to a container, given the container and object keys."
  [container-key obj-key]
  (let [container-obj (get-object container-key)
        old-contents (:contents container-obj)]
    (swap! objects assoc-in [container-key :contents] (conj old-contents obj-key))))

(defn remove-contents
  "Remove an object from a container, given the container and object key."
  [container-key obj-key]
  (let [container-obj (get-object container-key)
        old-contents (:contents container-obj)]
    (swap! objects assoc-in [container-key :contents] (disj old-contents obj-key))))

(defn remove-supports
  "Remove an object from a supporter, given the supporter and object key."
  [supporter-key obj-key]
  (let [supporter-obj (get-object supporter-key)
        old-supports (:supports supporter-obj)]
    (swap! objects assoc-in [supporter-key :supports] (disj old-supports obj-key))))

(defn remove-child
  "Remove a child object from a parent object, given the parent's key and the child's key."
  [parent-key child-key]
  (let [parent (get-object parent-key)]
    (if (contains? (:contents parent) child-key)
      (remove-contents parent-key child-key)
      (remove-supports parent-key child-key))))

(defn object-contains?
  "Returns whether an object contains another object, given their keys."
  [obj obj-key]
  (some #{obj-key} (:contents obj)))

(defn add-support
  "Adds an object to a supporting object, given the container and object keys."
  [supporter-key obj-key]
  (let [supporter-obj (get-object supporter-key)
        old-supports (:supports supporter-obj)]
    (assert (has-prop? supporter-obj :supporter))
    (swap! objects assoc-in [supporter-key :supports] (conj old-supports obj-key))))

(defn object-supports?
  "Returns whether an object supports another object, given their keys."
  [obj obj-key]
  (assert (has-prop? obj :supporter))
  (some #{obj-key} (:supports obj)))
