(ns ifgame.object)

(defn describe
  "Returns the description of an object. If verbosity is :full returns a full description, otherwise returns the short
  one."
  [object verbosity]
  (cond
    (contains? (:has object) :scenery)     nil
    (= verbosity :full)                    (str "There is a " (:full-description object) " here.")
    :else                                  (str "There is a " (:short-description object) " here.")))
