(ns tic-tac-toe.db)

(def height 5)
(def width 5)

(def default-db
  {:name "re-frame"
   :age 34
   :width width
   :height height
   :turn :O
   :winner nil
   :table (vec (repeat height (vec (repeat width nil))))})
