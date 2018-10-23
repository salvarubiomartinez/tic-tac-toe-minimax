(ns tic-tac-toe.views
  (:require
   [re-frame.core :as re-frame]
   [tic-tac-toe.subs :as subs]))

(defn main-panel []
  (let [winner (re-frame/subscribe [::subs/winner])
        table (re-frame/subscribe [::subs/table])
        width (re-frame/subscribe [::subs/width])
        height (re-frame/subscribe [::subs/height])]
    [:div
     [:table
      [:tbody
       (doall (for [i (range @height)]
                ^{:key (str "tr-" i)}
                [:tr (doall (for [j (range @width)]
                              ^{:key (str "td-" j)}
                              [:td
                               {:on-click #(re-frame/dispatch [:play i j])
                                :style {:border "1px solid black"
                                        :width "30px"
                                        :height "30px"
                                        :text-align "center"}}
                               (get-in @table [i j])]))]))]]
     [:br]
     [:button {:on-click #(re-frame/dispatch [:re-start])} "restart"]
     [:h3 "the winner is " @winner]]))
