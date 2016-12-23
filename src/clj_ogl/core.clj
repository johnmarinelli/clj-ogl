(ns clj-ogl.core
  (import [com.jogamp.newt Display NewtFactory Screen]
          [com.jogamp.newt.opengl GLWindow]
          [com.jogamp.opengl GL3 GL4 GLAutoDrawable GLCapabilities GLContext GLEventListener GLProfile]
          [com.jogamp.opengl.util GLBuffers Animator]
          [java.nio ByteBuffer FloatBuffer IntBuffer ShortBuffer]))

; https://jogamp.org/deployment/jogamp-next/javadoc/jogl/javadoc/com/jogamp/newt/Display.html
(defn create-display [^String name]
  (NewtFactory/createDisplay name))

; https://jogamp.org/deployment/jogamp-next/javadoc/jogl/javadoc/com/jogamp/newt/Screen.html
(defn create-screen [^Display display ^long index]
  (NewtFactory/createScreen display index))

; https://jogamp.org/deployment/v2.1.0/javadoc/jogl/javadoc/javax/media/opengl/GLProfile.html
(defn get-gl-profile [^String profile]
  (GLProfile/get profile))

; https://jogamp.org/deployment/v2.1.0/javadoc/jogl/javadoc/javax/media/opengl/GLCapabilities.html
(defn get-gl-capabilities [^GLProfile profile]
  (GLCapabilities. profile))

; https://jogamp.org/deployment/jogamp-next/javadoc/jogl/javadoc/com/jogamp/newt/NewtFactory.html#createWindow(java.lang.Object[], com.jogamp.newt.Screen, com.jogamp.nativewindow.CapabilitiesImmutable)
(defn create-window [^Screen screen ^GLCapabilities gl-capabilities]
  (GLWindow/create screen gl-capabilities))

(defn set-window-size [^GLWindow window ^long width ^long height]
  (.setSize window width height))

(defn go [args]
  (let [display (create-display nil)
        screen (create-screen display 0)
        gl-profile (get-gl-profile GLProfile/GL4)
        gl-cap (get-gl-capabilities gl-profile)
        gl-window (create-window screen gl-cap)
        width 1024
        height 768]
    (.setSize gl-window width height)
    (.setPosition gl-window 50 50)
    (.setUndecorated gl-window false)
    (.setAlwaysOnTop gl-window false)
    (.setVisible gl-window true)))

(def vertex-count 3)
(def vertex-size (* 5 vertex-count Float/BYTES))
(def vertex-data (float-array '(-1 -1 1 0 0
                              0  2 0 0 1
                              1 -1 0 1 0)))
(def element-count 3)
(def element-size (* Short/BYTES element-count))
(def element-data (short-array '(2 1 3)))

(def ^:const BUFFER
  (apply merge 
         (map #(identity {%1 %2}) 
              [:vertex :element :transform :max] 
              (range 0 4))))

(comment(def HelloTriangle
   (reify GLEventListener
     (display [this drawable]
       )
    
     (dispose [this drawable])
     (reshape [this drawable x y width height]
       (println "Reshape")
       (let [gl4 (-> drawable .getGL .getGL4)]
         (.glViewport gl4 x y width height)))

     (init [this drawable]
       (println "Init")
       (let [gl4 (-> drawable .getGL .getGL4)
             scale (make-array Float/TYPE 16)
             z-rotation (make-array Float/TYPE 16)
             model-to-clip (make-array Float/TYPE 16)
             start 0
             now 0
             buffer-name (GLBuffers/newDirectIntBuffer (:max BUFFER)) ; vao
             vertex-array-name (GLBuffers/newDirectIntBuffer 1)
             clear-color (GLBuffers/newDirectFloatBuffer (float-array [1.0 0.5 0.0 1.0]))
             clear-depth (GLBuffers/newDirectFloatBuffer (float-array [1.0]))]
         ((fn init-buffers [^GL4 gl4]
            (let [vertex-buffer (GLBuffers/newDirectFloatBuffer vertex-data)
                  element-buffer (GLBuffers/newDirectShortBuffer element-data)]
                                        ;             (.glCreateVertexArrays gl4 (:max BUFFER) buffer-name)
                                        ;             (.glCreateBuffers gl4 (:max BUFFER) buffer-name)
                                        ;             (.glNamedBufferStorage (.get buffer-name (:vertex BUFFER)) (* (.capacity vertex-buffer) Float/BYTES vertex-buffer com.jogamp.opengl.GL/GL_STATIC_DRAW))
              )) 
          gl4))))))

(def HelloTriangle
  (reify GLEventListener
    (display [this drawable])
    (dispose [this drawable])
    (reshape [this drawable x y width height]
      (let [gl4 (-> drawable .getGL .getGL4)]
        (.glViewport gl4 x y width height)))
    (init [this drawable]
      (let [gl4 (-> drawable .getGL .getGL4)
            scale (make-array Float/TYPE 16)
            model-to-clip (make-array Float/TYPE 16)
            start 0
            now 0
            buffer (GLBuffers/newDirectIntBuffer (:max BUFFER))
            vertex-array (GLBuffers/newDirectIntBuffer 1)
            clear-color (GLBuffers/newDirectFloatBuffer (float-array [1.0 0.5 0.0 1.0]))
            clear-depth (GLBuffers/newDirectFloatBuffer (float-array [1.0]))]
        (.glGenBuffers gl4 (:max BUFFER) buffer)
        (.glBindBuffer gl4 GL4/GL_ARRAY_BUFFER (.get buffer (:vertex BUFFER)))))))

(def display (create-display "john"))
(def screen (create-screen display 0))
(def profile (get-gl-profile GLProfile/GL4))
(def caps (get-gl-capabilities profile))
(def wdw (create-window screen caps))

(.setSize wdw 1024 768)
(.setPosition wdw 50 50)

(.addGLEventListener wdw HelloTriangle)

;(.start (Animator. wdw))

