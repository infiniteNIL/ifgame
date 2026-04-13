(ns ifgame.room
  (:require [ifgame.object :as object]))

(defrecord Room [name description
                 north south east west up down
                 northwest northeast southwest southeast
                 has])

(defn full-description [location]
  (str (:name @location) \newline (:description @location)))

(defn short-description [location]
  (:name @location))

(defn first-visit? [location]
  (= 1 (:visit-count @location 0)))

(defn describe-objects [location]
  (let [objects (:objects @location)]
    (when-not (empty? objects)
      (str (reduce (fn [description obj-key]
                     (let [desc (object/describe obj-key :short)]
                       (cond
                         (nil? desc)           description
                         (empty? description)  desc
                         :else                 (str description \newline desc))))
                   ""
                   (:objects @location))))))

(defn describe
  "Describe a room and it's contents. Verbosity is :full for the full description, otherwise a short description is
  returned."
  [location verbosity]
  (let [room-desc (if (= verbosity :full)
                    (full-description location)
                    (short-description location))
        object-descriptions (describe-objects location)]
    (if-not (empty? object-descriptions)
      (str room-desc \newline (describe-objects location))
      room-desc)))

(defn visit [location]
  (let [count (:visit-count @location 0)]
    (alter-var-root location assoc :visit-count (inc count))))


(defn objects [location]
  (:objects @location))

(defn add-object [location object-key]
  (let [new-objects (conj (objects location) object-key)]
    (alter-var-root location assoc :objects new-objects)))

(defn remove-object [location object-key]
  (let [new-objects (remove #{object-key} (objects location))]
    (alter-var-root location assoc :objects new-objects)))
