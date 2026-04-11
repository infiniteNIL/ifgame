(ns ifgame.objects)

(def ^:private objects
  {:bird {:short-description "baby bird"
          :full-description "Too young to fly, the nestling tweets helplessly."
          :names ["bird" "nestling"]
          :adjectives ["baby"]
          :has #{}}

   :nest {:short-description "bird's nest"
          :full-description "The nest is carefully woven of twigs and moss."
          :names ["nest" "twigs" "moss"]
          :adjectives ["bird"]
          :has #{:container :open}}

   :tree {:short-description "tall sycamore tree"
          :full-description "Standing proud in the middle of the clearing, the stout tree looks easy to climb."
          :names ["sycamore" "tree"]
          :adjectives ["tall" "stout" "proud"]
          :has #{:scenery}}

   :branch {:short-description "wide firm bough"
            :full-description "It's flat enough to support a small object."
            :names ["bough" "branch"]
            :adjectives ["wide" "firm" "flat"]
            :has #{:static :supporter}}})

(defn get-object-by-key [key]
  (key objects))

#_(defn get-object [name]
    (some (fn [obj] (if (some #{name} (:names obj)) obj nil))
          (vals objects)))

(defn get-object
  "Get's an object by name and returns a map with the key and object
  i.e.: {:key {key} :object object}"
  [name]
  (some (fn [key]
          (let [obj (key objects)]
            (if (some #{name} (:names obj))
              {:key key :object obj}
              nil)))
        (keys objects)))
