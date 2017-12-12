(ns nilernium.ui
  (:require [nilernium.layout :as l]
            [nilernium.renderer :refer [vdom-render]]))

(defn- header []
  {:tag :header,
   :id :site-header
   :layout {:left 0, :width (l/query-attr [['parent :layout :width]] identity)
            :top 0, :height 50}
   :style {:background-color "navy"}})

(defn- side-nav []
  {:tag :nav
   :id :site-nav
   :layout {:left 0, :width 300
            :top    (l/query-attr [[:site-header :layout :height]] identity)
            :height (l/query-attr [[:content :layout :height]] identity)}
   :style {:background-color "purple"}})

(defn- content []
  {:tag :article
   :id :content
   :layout {:left   (l/query-attr [[:site-nav :layout :width]] identity)
            :width  (l/query-attr [['parent :layout :width]
                                   [:site-nav :layout :width]] -)
            :top    (l/query-attr [[:site-header :layout :height]] identity)
            :height (l/content-height)}
   :style {:background-color "pink"}
   :children [{:tag :h1
               :layout false
               :children ["Hello World!"]}
              {:tag :p
               :layout false
               :children ["foo bar baz quux."]}]})

(defn- page [state]
  {:tag :div
   :id "app-root"
   :layout {:hcenter (/ (-> state :window :width) 2), :width (min (-> state :window :width) 960)
            :top 0, :height (-> state :window :height)}
   :children [(header) (content) (side-nav)]})

(defn render [state events]
  (l/render vdom-render (page state)))
