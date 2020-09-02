(ns lac.styles
  (:require
   [garden.core :as garden]))


(def stylesheet
  [
   [:div.wprm-recipe {:border "1px solid #e0e0e0"
                      :padding "1rem"}
    [:h3 {:margin-top "1rem"}]]
   [:a.wprm-recipe-jump {:display "inline-block"
                         :border "1px solid #e0e0e0"
                         :padding "0.5rem"
                         :margin-bottom "0.3rem"}]
   [:div.wrapper {:margin "0 auto"
                  :max-width "53rem"}]
   [:h1 :h2 :h3 :h4 {:font-family "sans-serif"}]
   [:figure {:margin "0 0 1.45rem"}]
   [:figcaption {:text-align "center"}]
   [:div.category-block {:background-color "aliceblue"
                         :padding "1rem"
                         :border-bottom "1px solid rgb(204, 204, 204)"
                         :margin-bottom "1rem"}
    [:div.byline {:margin-left "0.5rem"}]]
   [:img {:max-width "100%"
          :height "auto"}]
   [:ul {:padding-left "2rem"}]
   [:div.posted-by {:display "inline block"
                    :background-color "#f2f2f2"
                    :padding "0.5rem"
                    :margin "0.3rem 0"}]
   [:ul.post-list {:list-style "outside none none"
                   :margin "0 0 1.45rem 1.45rem"
                   :padding 0}
    [:li {:margin "1rem 0"
          :padding 0}]]
   [:div.wp-block-columns {:display "flex"
                           :flex-direction "row"
                           :justify-content "space-between"}]
   [:main {:padding "1rem 2rem 2rem 2rem"}]
   [:footer {:text-align "center"
             :font-size "0.75rem"
             :margin-bottom "3rem"}]
   [:header
    [:a.support-link {:text-decoration "none"
                      :color "white"
                      :font-size "1.2rem"}]
    [:div.banner-pic {:display "flex"
                      :justify-content "space-around"
                      :align-items "center"
                      :margin "0px auto"
                      :padding "1rem 2rem"}
     [:img {:height "5rem"}]]
    [:div.main-menu {:display "flex"
                     :justify-content "space-between"
                     :align-items "center"
                     :margin "0px auto"
                     :padding "0.6rem 1.8rem 0.6rem 1rem"
                     :background-color "rgb(51, 110, 132)"}
     [:ul {:display "flex"
           :list-style "none"
           :margin 0
           :padding 0
           :justify-content "space-around"}
      [:li {:margin "0 0.1rem"
            :padding "0 1rem"}
       [:a {:font-size "1.2rem"
            :color "white"
            :text-decoration "none"
            :font-family "sans-serif"
            :whitespace "nowrap"}]]]]]])


(defn spit-styles
  [path]
  (garden/css {:output-to path} stylesheet))
