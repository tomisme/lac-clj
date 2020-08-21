(ns site.fetch
  (:require
   [clojure.edn :as edn]
   [clojure.data.json :as json]
   [clj-http.lite.client :as http]))


(def config
  (edn/read-string (slurp "config.edn")))


(def all-posts-query "{posts{edges{node{id,modified,modifiedGmt,title,status,slug,uri,date}}}}")


(def posts-response
  (http/post (:graphql-endpoint config)
             {:content-type :json
              :accept :json
              :body (str "{\"query\": \"" all-posts-query "\"}")}))


(def post-responses
  (-> posts-response
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

#_(doseq [post (map process-post-response post-responses)]
    (spit (str "data/posts/" (:post/id post) ".edn")
          post))
