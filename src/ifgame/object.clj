(ns ifgame.object
  (:require [ifgame.objects :as objects]))

(defn describe
  "Returns the description of an object. If verbosity is :full returns a full description, otherwise returns the short
  one."
  [object-key verbosity]
  (let [object (objects/get-object-by-key object-key)]
    (cond
      (contains? (:has object) :scenery)     nil
      (= verbosity :full)                    (str "There is a " (:full-description object) " here.")
      :else                                  (str "There is a " (:short-description object) " here."))))

(defn get-names [object-key]
  (let [obj (objects/get-object-by-key object-key)]
    (:names obj)))

(defn get-adjectives [object-key]
  (let [obj (objects/get-object-by-key object-key)]
    (:adjectives obj)))

(defn takeable? [object]
  (not (or (contains? (:has object) :scenery)
           (contains? (:has object) :static))))
