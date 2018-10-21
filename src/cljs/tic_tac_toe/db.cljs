(ns tic-tac-toe.db)

(def default-db
  {:name "re-frame"
   :age 34
   :turn :O
   :winner nil
   :table [
           [nil nil nil]
           [nil nil nil]
           [nil nil nil]]})
