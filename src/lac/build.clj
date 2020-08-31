(ns lac.build
  (:require
   [portal.api :as p]
   [lac.elements :as el]
   [clojure.edn :as edn]
   [clojure.data.json :as json]
   [hickory.core :as hickory]
   [hiccup.core :as hiccup]
   [clj-http.lite.client :as http]
   [me.raynes.fs :as fs]
   #_[tick.alpha.api :as t]))

(def config
  (edn/read-string (slurp "config.edn")))


(def all-posts-query
  "{posts{edges{node{id,modified,modifiedGmt,title,status,slug,uri,date,categories{nodes{name}}}}}}")


(def main-menu-query
  "{menuItems(where:{location:MENU_1}){nodes{path,order,label}}}")


(def categories-query
  "{categories{edges{node{description,name,uri,children{nodes{name}}}}}}")


(defn post-content-by-id-query
  [id]
  (str "{post(id:\"" id "\"){content}}"))


(defn posts-by-category-query
  [category-name]
  (str "{posts(where:{categoryName:\"" category-name \""}){edges{node{id,uri}}}}"))


(defn query!
  [q]
  (-> (http/post (:graphql-endpoint config)
                 {:content-type :json
                  :accept :json
                  :body (json/write-str {:query q})})
      :body
      (json/read-str :key-fn keyword)
      :data))


(def data-path "data")


(def main-menu-path
  (str data-path "/wp-main-menu.edn"))


(def categories-path
  (str data-path "/wp-categories.edn"))


(def post-content-path
  (str data-path "/wp-post-content"))


(defn post-content-path-by-id
  [id]
  (str post-content-path "/" id ".html"))


(def post-meta-path
  (str data-path "/wp-post-meta"))


(defn post-meta-path-by-id
  [id]
  (str post-meta-path "/" id ".edn"))


(defn post-res->post-meta
  [{:keys [node]}]
  {:post/id (:id node)
   :categories (-> node :categories :nodes)
   :published-date (:date node)
   :last-modified-date-gmt (:modifiedGmt node)
   :uri (:uri node)
   :slug (:slug node)
   :title (:title node)})


(defn dl-post-metas!
  []
  (fs/mkdirs post-meta-path)
  (doseq [meta (map post-res->post-meta
                    (-> (query! all-posts-query)
                        :posts
                        :edges))]
    (spit (post-meta-path-by-id (:post/id meta))
          meta)))


(defn dl-post-content!
  [id]
  (fs/mkdirs post-content-path)
  (spit (post-content-path-by-id id)
        (-> (post-content-by-id-query id)
            query!
            :post
            :content)))


(defn process-menu-items
  [coll]
  (sort-by :order coll))


(defn dl-main-menu!
  []
  (spit main-menu-path
        (-> (query! main-menu-query)
            :menuItems
            :nodes
            process-menu-items)))


(defn dl-categories!
  []
  (spit categories-path
        (->> (query! categories-query)
            :categories
            :edges
            (map :node)
            (map (fn [category]
                   (let [children (-> category :children :nodes)]
                     (if (seq children)
                       (assoc category :children children)
                       (dissoc category :children)))))
            seq)))


(defn get-saved-post-meta-ids
  []
  (map fs/name (fs/list-dir post-meta-path)))


(defn get-saved-post-metas
  []
  (for [f (fs/list-dir post-meta-path)]
    (edn/read-string (slurp f))))


(defn get-saved-main-menu
  []
  (edn/read-string (slurp main-menu-path)))


(defn get-saved-categories
  []
  (edn/read-string (slurp categories-path)))


(defn get-saved-category-details-by-name
  [name]
  (some #(when (= name (:name %))
           %)
        (get-saved-categories)))


(defn get-saved-posts-by-category-name
  [name]
  (filter (fn [{:keys [categories]}]
            (some #{name} (map :name categories)))
          (get-saved-post-metas)))


(defn build-post-content-hiccup
  [id]
  (map hickory/as-hiccup
       (-> (post-content-path-by-id id)
           slurp
           (hickory/parse-fragment))))


(defn build-page-hiccup
  [{:keys [main]}]
  [:body
   (el/header-el {:menubar-items (get-saved-main-menu)
                  :header-img-url "https://lightsandclockwork.xyz/static/header-6de1aeeca65f723a4779953f735d7b4d.svg"})
   (el/main-el main)
   (el/footer-el)])


(defn build-post-page-hiccup
  [id]
  (build-page-hiccup {:main (build-post-content-hiccup id)}))


(defn category-child-el
  [{:keys [name]}]
  (let [{:keys [description]} (get-saved-category-details-by-name name)]
    [:div
     [:h2 name]
     [:p description]]))


(defn post-preview-el
  [{:keys [title uri]}]
  [:li
   [:a {:href uri}
    title]])


(defn category-page-main-el
  [{:keys [description name children]}]
  [:div
   [:h1 name]
   [:p description]
   (if children
     (into [:div]
           (map category-child-el children))
     (into [:ul]
           (map post-preview-el (get-saved-posts-by-category-name name))))])


(defn build-category-page-hiccup
  [category]
  (build-page-hiccup {:main (category-page-main-el category)}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


#_(p/open)
#_(p/tap)

#_(post-content-path-by-id "cG9zdDo0MA==")
#_(dl-post-content! "cG9zdDo0MA==")
#_(tap> (build-post-content-hiccup "cG9zdDo0MA=="))
#_(tap> (build-post-page-hiccup "cG9zdDo0MA=="))
#_(tap> (-> (build-post-page-hiccup "cG9zdDo0MA==")
            hiccup/html))
#_(tap> (get-saved-post-meta-ids))
#_(tap> (for [category (get-saved-categories)]
          (build-category-page-hiccup category)))


;; download wordpress data
#_(dl-post-metas!)
#_(dl-main-menu!)
#_(dl-categories!)

;; download wordpress post content
#_(doseq [id (get-saved-post-meta-ids)]
    (dl-post-content! id))

;; build category pages
#_(doseq [{:keys [uri] :as category} (get-saved-categories)]
    (let [path (str "public" uri)]
      (fs/mkdirs path)
      (spit (str path "index.html")
            (-> (build-category-page-hiccup category)
                hiccup/html))))

;; build post pages
#_(doseq [{:keys [uri post/id]} (get-saved-post-metas)]
    (let [path (str "public" uri)]
      (fs/mkdirs path)
      (spit (str path "index.html")
            (-> (build-post-page-hiccup id)
                hiccup/html))))
