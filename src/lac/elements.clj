(ns lac.elements)


(defn header-el
  [{:keys [menubar-items header-img-url]}]
  [:header
   [:div {:style {:margin 0
                  :max-width 960
                  :padding "1rem 2rem"
                  :display "flex"
                  :justify-content "space-between"
                  :align-items "center"}}
    [:a {:style {:color "white"
                 :background-color "transparent"
                 :text-decoration "none"}}
     [:img {:style {:height "5rem"
                    :max-width "100%"
                    :margin-left 0
                    :margin-right 0
                    :margin-top 0
                    :border-style "none"}
            :src header-img-url}]]]
   [:div
    (into [:ul]
          (for [{:keys [path label]} menubar-items]
            [:li
             [:a {:href path}
              label]]))]])


(defn posted-by-el
  [{:keys [author-name author-link category-name category-link posted-date]}]
  [:div
   "Posted in"
   [:a {:href category-link}
    category-name]
   "by"
   [:a {:href author-link}
    author-name]
   "on"
   posted-date])


(defn post-list-el
  [{:keys [posts]}]
  (into [:ul]
        (for [{:keys [title excerpt] :as post} posts]
          [:li
           [:h3 title]
           (posted-by-el post)
           [:p excerpt]])))


(defn main-el
  [& children]
  (into [:main] children))


(defn footer-el
  []
  [:footer
   [:div
    "Thanks for visiting 😃"]
   [:div
    [:a {:rel "license"
         :href "http://creativecommons.org/licenses/by-nc-sa/4.0/"}
     [:img {:alt "Creative Commons License"
            :style {:border-width 0}
            :src "https://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png"}]]
    [:div
     "This page is licensed under a "
     [:a {:rel "license"
          :href "http://creativecommons.org/licenses/by-nc-sa/4.0/"}
      "Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License"]]
    [:div
     "That means it's free to read, share, adapt and reproduce
      for non-commercial purposes, as long as any reproductions
      are also released under the same license, and you give credit."]]])
