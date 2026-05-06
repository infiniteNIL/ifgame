(ns ifgame.action
  "Provides functions for dealing with an action"
  (:require [clojure.set :as set]))

(def ^:private actions (atom {}))

;; TODO: Need more specs on forms
(defn action
  "Define an action for the game.
     id - the keyword for the action in actions
     synonyms - vector of words the action goes by.
     requires is a set of flags the direct object must have for the action to work.
     forms is a list of forms that action can take such as:
        [\"read\" :direct-object
         \"read\" :direct-object \"with\" :indirect-object
         \"read\" :direct-object \"through\" :indirect-object
     handler is the function to handle the action. It should return true if it actually handles it.
     and no further processing should happen."
  [id synonyms & {:keys [requires forms handler]
                  :or {requires #{}
                       handler nil
                       forms #{}}}]
  (swap! actions assoc id {:type :action
                           :synonyms synonyms
                           :requires requires
                           :forms forms
                           :handler handler}))

(defn get-action-by-word [word]
  (let [key (->> @actions
                 (keys)
                 (filter #(some #{word} (:synonyms (%1 @actions))))
                 first)]
    (get @actions key)))

(defn get-action-by-key [key]
  (get @actions key))

(defn is-action? [action key]
  (= action (get-action-by-key key)))

(comment
  (action "take"
          :synonyms ["get" "pick up"]))
