(ns ifgame.room
  (:require [ifgame.object :as object]))

(defrecord Room [name description
                 north south east west up down
                 northwest northeast southwest southeast
                 has])

#_(defn- exits-description [location]
    (let [exits (keys (:exits location))
          exit-count (count exits)]
      (case exit-count
        0 "There are no exits."
        1 (str "The only exit is to the " (first exits) ".")
        2 (str "There are exits to the " (first exits) " and " (second exits) ".")
        (let [last-exit (last exits)
              exits-str (str/join ", " (butlast exits))]
          (str "There are exits to the " exits-str " and " last-exit ".")))))

(defn full-description [location]
  (str (:name @location) \newline (:description @location)))

(defn short-description [location]
  (:name @location))

(defn first-visit? [location]
  (= 1 (:visit-count @location 0)))

(defn describe-objects [location]
  (let [objects (:objects @location)]
    (when objects
      (str (reduce (fn [description obj]
                     (let [desc (object/describe @obj :short)]
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
                    (short-description location))]
    (if (:objects @location)
      (str room-desc \newline (describe-objects location))
      room-desc)))

(defn visit [location]
  (let [count (:visit-count @location 0)]
    (alter-var-root location assoc :visit-count (inc count))))
