(ns lac.elements
  (:require
   [tick.alpha.api :as t]))


(defn header-el
  [{:keys [menubar-items header-img-url]}]
  [:header
   [:div.banner-pic
    [:a#page-top {:href "/"}
     [:img {:src header-img-url}]]]
   [:div.main-menu
    (into [:ul]
          (for [{:keys [path label]} menubar-items]
            [:li
             [:a {:href path}
              label]]))
    [:a.support-link {:href "/support"}
     "❤"]]])



(defn main-el
  [& children]
  (into [:main] children))


(defn footer-el
  []
  [:footer
   [:p
    [:a {:href "#page-top"}
     "Jump back to top"]]
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
    [:div.posted-by
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
    (into [:ul.post-list]
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
  [:div.category-block
   [:a {:href uri}
    [:h2 name]]
   [:div.byline "By Elisa"]
   [:p description]])


(defn basic-main-el
  [{:keys [title]} content]
  [:div
   [:h1 title]
   content])


(defn category-page-main-el
  [{:keys [category posts children]}]
  (let [{:keys [description name]} category]
    [:div
     [:h1 (condp = name
            "Blog"    "Blog Posts"
            "Recipes" "Latest Recipes"
            name)]
     [:p description]
     (if (seq children)
       (into [:div]
             (map category-child-el children))
       (post-list-el posts))]))
