(ns lac.build
  (:require
   [lac.elements :as el]
   [lac.styles :as styles]
   [portal.api :as p]
   [clojure.edn :as edn]
   [clojure.data.json :as json]
   [hickory.core :as hickory]
   [hiccup.core :as hiccup]
   [hiccup.page :as hiccup-page]
   [clj-http.lite.client :as http]
   [me.raynes.fs :as fs]
   [tick.alpha.api :as t]
   [time-literals.read-write]))


(defn tp [x] (tap> x) x)


(defn read-edn
  [filename]
  (edn/read-string
   {:readers time-literals.read-write/tags}
   (slurp filename)))


(def config
  (read-edn "config.edn"))


(def all-posts-query
  "{posts{edges{node{details{excerpt,previewImage{sourceUrl}},id,modified,modifiedGmt,title,status,slug,uri,date,author{node{name}},categories{nodes{name,uri}}}}}}")


(def all-pages-query
  "{pages{nodes{id,uri,title}}}")


(def main-menu-query
  "{menuItems(where:{location:MENU_1}){nodes{path,order,label}}}")


(def categories-query
  "{categories{edges{node{description,name,uri,children{nodes{name}}}}}}")


(defn post-content-by-id-query
  [id]
  (str "{post(id:\"" id "\"){content}}"))


(defn page-content-by-id-query
  [id]
  (str "{page(id:\"" id "\"){content}}"))


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


(def public-path "public")


(def assets-path "assets")


(def styles-filename "styles.css")


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


(def post-excerpt-path
  (str data-path "/wp-post-excerpt"))


(defn post-excerpt-path-by-id
  [id]
  (str post-excerpt-path "/" id ".edn"))


(def page-content-path
  (str data-path "/wp-page-content"))


(defn page-content-path-by-id
  [id]
  (str page-content-path "/" id ".html"))


(def page-meta-path
  (str data-path "/wp-page-meta"))


(defn page-meta-path-by-id
  [id]
  (str page-meta-path "/" id ".edn"))


(defn post-res->post-meta
  [{:keys [node]}]
  {:post/id (:id node)
   :excerpt (-> node :details :excerpt)
   :preview-image-url (-> node :details :previewImage :sourceUrl)
   :author (-> node :author :node)
   :categories (-> node :categories :nodes)
   :published-date (t/parse (:date node))
   :last-modified-date-gmt (t/parse (:modifiedGmt node))
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


(defn page-res->page-meta
  [{:keys [uri id title]}]
  {:page/id id
   :uri uri
   :title title})


(defn dl-page-metas!
  []
  (fs/mkdirs page-meta-path)
  (doseq [meta (map page-res->page-meta
                    (-> (query! all-pages-query)
                        :pages
                        :nodes))]
    (spit (page-meta-path-by-id (:page/id meta))
          meta)))


(defn dl-post-content!
  [id]
  (fs/mkdirs post-content-path)
  (spit (post-content-path-by-id id)
        (-> (post-content-by-id-query id)
            query!
            :post
            :content)))


(defn dl-page-content!
  [id]
  (fs/mkdirs page-content-path)
  (spit (page-content-path-by-id id)
        (-> (page-content-by-id-query id)
            query!
            :page
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
  (map read-edn (fs/list-dir post-meta-path)))


(defn get-saved-page-meta-ids
  []
  (map fs/name (fs/list-dir page-meta-path)))


(defn get-saved-page-metas
  []
  (map read-edn (fs/list-dir page-meta-path)))


(defn get-saved-post-meta-by-id
  [id]
  (read-edn (post-meta-path-by-id id)))


(defn get-saved-post-excerpt-by-id
  [id]
  (read-edn (post-excerpt-path-by-id id)))


(defn get-saved-page-meta-by-id
  [id]
  (read-edn (page-meta-path-by-id id)))


(defn get-saved-main-menu
  []
  (read-edn main-menu-path))


(defn get-saved-categories
  []
  (read-edn categories-path))


(defn get-saved-category-details-by-name
  [name]
  (some #(when (= name (:name %))
           %)
        (get-saved-categories)))


(defn get-saved-post-metas-by-category-name
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


(defn build-page-content-hiccup
  [id]
  (map hickory/as-hiccup
       (-> (page-content-path-by-id id)
           slurp
           (hickory/parse-fragment))))


(defn p-form-content
  [form]
  (let [[k _ content] form]
    (when (= k :p)
      content)))


(defn make-post-excerpt
  [id]
  (some p-form-content (build-post-content-hiccup id)))


(defn build-page-hiccup
  [{:keys [main]}]
  (hiccup-page/html5 {:lang "en"}
    [:head
     (hiccup-page/include-css (str "/" styles-filename))
     (hiccup-page/include-css "/recipe-public-modern.css")]
    [:body
     [:div.wrapper
      (el/header-el {:menubar-items (get-saved-main-menu)
                     :header-img-url "https://lightsandclockwork.xyz/static/header-6de1aeeca65f723a4779953f735d7b4d.svg"})
      (el/main-el main)
      (el/footer-el)]]))


(defn build-post-page-hiccup
  [id]
  (build-page-hiccup
   {:main (el/post-main-el
            (get-saved-post-meta-by-id id)
            (build-post-content-hiccup id))}))


(defn build-page-page-hiccup
  [id]
  (build-page-hiccup
   {:main (el/page-main-el
           (get-saved-page-meta-by-id id)
           (build-page-content-hiccup id))}))


(defn build-index-page-hiccup
  []
  (build-page-hiccup
   {:main (el/index-page-main-el
           (->> (get-saved-post-metas)
                (sort-by :published-date t/>)))}))


(defn build-category-page-hiccup
  [{:keys [name children] :as category}]
  (let [posts (get-saved-post-metas-by-category-name name)]
    (build-page-hiccup
     {:main (el/category-page-main-el
             {:category category
              :posts posts
              :children (map get-saved-category-details-by-name
                             (map :name children))})})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


#_(p/open)
#_(p/tap)

#_(tap> (post-content-path-by-id "cG9zdDo0MA=="))
#_(tap> (dl-post-content! "cG9zdDo0MA=="))
#_(tap> (build-post-content-hiccup "cG9zdDo0MA=="))
#_(tap> (build-post-page-hiccup "cG9zdDo0MA=="))
#_(tap> (-> (build-post-page-hiccup "cG9zdDo0MA==")
            hiccup/html))
#_(tap> (get-saved-post-meta-ids))
#_(tap> (for [category (get-saved-categories)]
          (build-category-page-hiccup category)))
#_(tap> (-> (build-category-page-hiccup {:name "Fiction"})))
#_(tap> (->> (get-saved-post-metas)
             first
             :published-date
             (t/format "MMM d, yyy")))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; copy assets
#_(fs/copy-dir-into assets-path public-path)

;; download wordpress data
#_(dl-post-metas!)
#_(dl-page-metas!)
#_(dl-main-menu!)
#_(dl-categories!)

;; download wordpress post content
#_(doseq [id (get-saved-post-meta-ids)]
    (dl-post-content! id))

;; build post excerpts
;; TODO only overwrite posts with no excerpts
; #_(fs/mkdirs post-excerpt-path)
; #_(doseq [id (get-saved-post-meta-ids)]
;     (spit (post-excerpt-path-by-id id)
;           {:excerpt (make-post-excerpt id)}))

;; merge excerpts into post metas
#_(doseq [{:keys [post/id] :as meta} (tp (get-saved-post-metas))]
    (spit (post-meta-path-by-id id)
          (merge meta (get-saved-post-excerpt-by-id id))))

;; download wordpress page content
#_(doseq [id (get-saved-page-meta-ids)]
    (dl-page-content! id))

;; build stylesheet
#_(styles/spit-styles (str public-path "/" styles-filename))

;; build index page
#_(spit (str public-path "/index.html")
        (-> (build-index-page-hiccup)
            hiccup/html))

;; build category pages
#_(doseq [{:keys [uri] :as category} (get-saved-categories)]
    (let [path (str public-path uri)]
      (fs/mkdirs path)
      (spit (str path "index.html")
            (-> (build-category-page-hiccup category)
                hiccup/html))))

;; build post pages
#_(doseq [{:keys [uri post/id]} (get-saved-post-metas)]
    (let [path (str public-path uri)]
      (fs/mkdirs path)
      (spit (str path "index.html")
            (-> (build-post-page-hiccup id)
                hiccup/html))))

;; build page pages
#_(doseq [{:keys [uri page/id]} (get-saved-page-metas)]
    (let [path (str public-path uri)]
      (fs/mkdirs path)
      (spit (str path "index.html")
            (-> (build-page-page-hiccup id)
                hiccup/html))))
