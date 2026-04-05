(ns ifgame.objects)

(def bird {:names ["baby" "bird" "nestling"]
           :short-description "baby bird"
           :description "Too young to fly, the nestling tweets helplessly."
           :has #{}})

(def nest {:names ["bird's" "nest" "twigs" "moss"]
           :short-description "bird's nest"
           :full-description "The nest is carefully woven of twigs and moss."
           :has #{:container :open}})

(def tree {:names ["tall" "sycamore" "tree" "stout" "proud"]
           :short-description "tall sycamore tree"
           :full-description "Standing proud in the middle of the clearing, the stout tree looks easy to climb."
           :has #{:scenery}})

(def branch {:names ["wide" "firm" "flat" "bough" "branch"]
             :short-description "wide firm bough"
             :full-description "It's flat enough to support a small object."
             :has #{:static :supporter}})
