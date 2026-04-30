(ns example.rooms
  (:require [ifgame.room :refer [room]]))

(room :before-cottage
      "In front of a cottage"
      "You stand outside a cottage. The forest stretches east."
      :east :forest
      :props #{:light})

(room :forest
      "Deep in the forest"
      "Through the dense foliage, you glimpse a building to the west. A track heads to the northeast."
      :west :before-cottage
      :northeast :clearing
      :props #{:light}
      :objects [:bird])

(room :clearing
      "A forest clearing"
      "A tall sycamore stands in the middle of this clearing. The path winds southwest through the trees."
      :southwest :forest
      :up :top-of-tree
      :props #{:light}
      :objects [:nest :tree])

(room :top-of-tree
      "At the top of the tree"
      "You cling precariously to the trunk."
      :down :clearing
      :props #{:light}
      :objects [:branch])
