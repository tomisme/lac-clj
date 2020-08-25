(ns lac.build
  (:require
   [lac.elements :as el]
   [clojure.edn :as edn]
   [clojure.data.json :as json]
   [hickory.core :as html]
   [hiccup.core :as hiccup]
   [clj-http.lite.client :as http]
   [me.raynes.fs :as fs]
   #_[tick.alpha.api :as t]))


(def config
  (edn/read-string (slurp "config.edn")))


(def all-posts-query "{posts{edges{node{id,modified,modifiedGmt,title,status,slug,uri,date}}}}")


(defn post-content-by-id-query
  [id]
  (str "{post(id:\"" id "\"){content}}"))


(defn query!
  [q]
  (http/post (:graphql-endpoint config)
             {:content-type :json
              :accept :json
              :body (json/write-str {:query q})}))


(def post-responses
  (-> (query! all-posts-query)
      :body
      (json/read-str :key-fn keyword)
      :data
      :posts
      :edges))


(defn process-post-response
  [{:keys [node]}]
  {:post/id (:id node)
   :published-date (:date node)
   :last-modified-date-gmt (:modifiedGmt node)
   :uri (:uri node)
   :slug (:slug node)
   :title (:title node)})


(defn wp-post-content-path
  [id]
  (str "data/wp_post_content/" id ".html"))


(defn wp-post-meta-path
  [id]
  (str "data/wp_post_meta/" id ".edn"))


(defn dl-wp-post-content!
  [id]
  (let [content (-> (post-content-by-id-query id)
                    query!
                    :body
                    (json/read-str :key-fn keyword)
                    :data
                    :post
                    :content)]
    (spit (wp-post-content-path id)
          content)))


(defn wp-post-content->hiccup
  [id]
  (let [seq (-> (wp-post-content-path id)
                slurp
                (html/parse-fragment))]
    (map html/as-hiccup seq)))


(defn get-saved-post-ids
  []
  (map fs/name (fs/list-dir "data/wp_post_meta")))


(defn build-post-page-hiccup
  [id]
  [:body
   (el/header-el {:menubar-items [{:name "Blog"}]
                  :header-img-url "https://lightsandclockwork.xyz/static/header-6de1aeeca65f723a4779953f735d7b4d.svg"})
   (el/main-el
       (wp-post-content->hiccup id))
   (el/footer-el)])


#_(wp-post-content-path "cG9zdDo0MA==")
#_(dl-wp-post-content! "cG9zdDo0MA==")
#_(into [:div] (wp-post-content->hiccup "cG9zdDo0MA=="))
#_(build-post-page-hiccup "cG9zdDo0MA==")

#_(spit "a.html"
        (-> (build-post-page-hiccup "cG9zdDo0MA==")
            hiccup/html))

#_(get-saved-post-ids)

#_(doseq [post (map process-post-response post-responses)]
    (spit (wp-post-meta-path (:post/id post))
          post))

#_(doseq [id (get-saved-post-ids)]
    (spit (str "public/post/")))
