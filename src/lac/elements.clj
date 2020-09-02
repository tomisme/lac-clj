(ns lac.elements
  (:require
   [tick.alpha.api :as t]))

#_{:style {:margin 0
           :max-width 960
           :padding "1rem 2rem"
           :display "flex"
           :justify-content "space-between"
           :align-items "center"}}
#_#_:style {:color "white"
            :background-color "transparent"
            :text-decoration "none"}
#_#_:style {:height "5rem"
            :max-width "100%"
            :margin-left 0
            :margin-right 0
            :margin-top 0
            :border-style "none"}

(defn header-el
  [{:keys [menubar-items header-img-url]}]
  [:header
   [:div
    [:a {:href "/"}
     [:img {:src header-img-url}]]]
   [:div
    (into [:ul]
          (for [{:keys [path label]} menubar-items]
            [:li
             [:a {:href path}
              label]]))]])


(defn main-el
  [& children]
  (into [:main] children))


(defn footer-el
  []
  [:footer
   [:p
    "Thanks for visiting 😃"]
   [:p
    [:a {:rel "license"
         :href "http://creativecommons.org/licenses/by-nc-sa/4.0/"}
     [:img {:alt "Creative Commons License"
            :style {:border-width 0}
            :src "https://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png"}]]
    [:p
     "This page is licensed under a "
     [:a {:rel "license"
          :href "http://creativecommons.org/licenses/by-nc-sa/4.0/"}
      "Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License"]]
    [:p
     "That means it's free to read, share, adapt and reproduce
      for non-commercial purposes, as long as any reproductions
      are also released under the same license, and you give credit."]]])


(defn posted-by-el
  [{:keys [author categories published-date]}]
  (let [category (first categories)]
    [:div
     "Posted in "
     [:a {:href (:uri category)}
      (:name category)]
     " by "
     [:a {:href nil}
      (:name author)]
     " on "
     (t/format "MMM d, yyy" published-date)]))


(defn post-list-el
  [posts]
  (if (empty? posts)
    [:div "Nothing here yet!"]
    (into [:ul]
          (for [{:keys [title excerpt uri] :as post} posts]
            [:li
             [:a {:href uri}
              [:h3 title]]
             (posted-by-el post)
             [:p excerpt]]))))


(defn index-page-main-el
  [post-metas]
  [:div
   [:h1 "Recent Posts"]
   (post-list-el post-metas)])


(defn category-child-el
  [{:keys [name description uri]}]
  [:div
   [:a {:href uri}
    [:h2 name]]
   [:p description]])


(defn basic-main-el
  [{:keys [title]} content]
  [:div
   [:h1 title]
   content])


(defn category-page-main-el
  [{:keys [description name children]} posts child-categories]
  [:div
   [:h1 (condp = name
          "Fiction" "Fiction Categories"
          "Blog"    "Blog Posts"
          "Recipes" "Latest Recipes"
          name)]
   [:p description]
   (if children
     (into [:div]
           (map category-child-el child-categories))
     (post-list-el posts))])
