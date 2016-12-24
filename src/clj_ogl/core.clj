(ns clj-ogl.core
  (import [com.jogamp.newt Display NewtFactory Screen]
          [com.jogamp.newt.opengl GLWindow]
          [com.jogamp.opengl GL3 GL4 GLAutoDrawable GLCapabilities GLContext GLEventListener GLProfile]
          [com.jogamp.opengl.util GLBuffers Animator]
          [com.jogamp.opengl.util.glsl ShaderCode ShaderProgram]
          [com.jogamp.opengl.math FloatUtil]
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

(def buffer-name (GLBuffers/newDirectIntBuffer (:max BUFFER)))

(def vertex-array-name (GLBuffers/newDirectIntBuffer 1))

(def ^:const BUFFER
  (apply merge 
         (map #(identity {%1 %2}) 
              [:vertex :element :transform :max] 
              (range 0 4))))

(defprotocol InitBufferProtocol
  (init-buffer [this ^GL4 gl4]))

(defprotocol InitVertexArrayProtocol
  (init-vertex-array [this ^GL4 gl4]))

(defprotocol InitGLSLProgramProtocol
  (init-glsl-program [this ^GL4 gl4]))

(def shaders-root "src/clj_ogl")
(def attr-position 0)
(def attr-frag-color 0)
(def attr-color 3) 
(def mv-matrix-ul (atom -1))
(def program-name (atom -1))
(def start (atom (System/currentTimeMillis)))
(def scale (atom (float-array 16 0)))
(def z-rotation (atom (float-array 16 0)))

(def HelloTriangle
  (reify 
    GLEventListener
    (display [this drawable]
      (let [gl4 (-> drawable .getGL .getGL4)]
        (.glClearColor gl4 0.0 0.33 0.66 1.0)
        (.glClearDepth gl4 1.0)
        (.glClear gl4 (bit-or GL4/GL_COLOR_BUFFER_BIT GL4/GL_DEPTH_BUFFER_BIT))
        (let [now (System/currentTimeMillis)
              diff (/ (- now @start) 1000.0)
              scl (FloatUtil/makeScale @scale true 0.5 0.5 0.5)
              z-rot (FloatUtil/makeRotationEuler @z-rotation 0 0 0 diff)
              model-to-clip (FloatUtil/multMatrix scl z-rot)]
          (.glUseProgram gl4 @program-name)
          (.glBindVertexArray gl4 (.get vertex-array-name 0))
          (.glUniformMatrix4fv gl4 @mv-matrix-ul 1 false model-to-clip 0)
          (.glDrawElements gl4 GL4/GL_TRIANGLES element-size GL4/GL_UNSIGNED_SHORT 0))))

    (dispose [this drawable]
      (let [gl4 (-> drawable .getGL .getGL4)]
        (.glDeleteProgram gl4 @program-name)))

    (reshape [this drawable x y width height]
      (let [gl4 (-> drawable .getGL .getGL4)]
        (println "reshape")
        (.glViewport gl4 x y width height)))

    (init [this drawable]
      (let [gl4 (-> drawable .getGL .getGL4)
            scale (make-array Float/TYPE 16)
            model-to-clip (make-array Float/TYPE 16)
            start 0
            now 0
            buffer (GLBuffers/newDirectIntBuffer (:max BUFFER))
            vertex-array (GLBuffers/newDirectIntBuffer 1)
            clear-color (GLBuffers/newDirectFloatBuffer 
                         (float-array [1.0 0.5 0.0 1.0]))
            clear-depth (GLBuffers/newDirectFloatBuffer (float-array [1.0]))]
        (println "init")
        (init-buffer this gl4)
        (init-vertex-array this gl4)
        (init-glsl-program this gl4)))

    InitBufferProtocol
    (init-buffer [this gl4]
      (let [vertex-buffer (GLBuffers/newDirectFloatBuffer vertex-data)
            element-buffer (GLBuffers/newDirectShortBuffer element-data)]
        (println "init-buffer")
        (println vertex-buffer)
        (.glGenBuffers gl4 (:max BUFFER) buffer-name)
        (.glBindBuffer gl4 GL4/GL_ARRAY_BUFFER (.get buffer-name (:vertex BUFFER)))
        (.glBufferData gl4 GL4/GL_ARRAY_BUFFER vertex-size vertex-buffer GL4/GL_STATIC_DRAW)
        (.glBindBuffer gl4 GL4/GL_ARRAY_BUFFER 0)
        
        (.glBindBuffer gl4 GL4/GL_ELEMENT_ARRAY_BUFFER (.get buffer-name (:element BUFFER)))
        (.glBufferData gl4 GL4/GL_ELEMENT_ARRAY_BUFFER element-size element-buffer GL4/GL_STATIC_DRAW)
        (.glBindBuffer gl4 GL4/GL_ELEMENT_ARRAY_BUFFER 0)))
    
    InitVertexArrayProtocol
    (init-vertex-array [this gl4]
      (.glGenVertexArrays  gl4 1 vertex-array-name)
      (.glBindVertexArray gl4 (.get vertex-array-name 0))
      (.glBindBuffer gl4 GL4/GL_ARRAY_BUFFER (.get buffer-name (:vertex BUFFER)))
      (let [stride (* 5 Float/BYTES)
            offset 0]
        (.glEnableVertexAttribArray gl4 attr-position)
        (.glVertexAttribPointer gl4 attr-position 2 GL4/GL_FLOAT false stride offset)
        (let [offset (* 2 Float/BYTES)]
          (.glEnableVertexAttribArray gl4 attr-color)
          (.glVertexAttribPointer gl4 attr-color 3 GL4/GL_FLOAT false stride offset))
        (.glBindBuffer gl4 GL4/GL_ARRAY_BUFFER 0)
        (.glBindBuffer gl4 GL4/GL_ELEMENT_ARRAY_BUFFER (.get buffer-name (:element BUFFER)))
        (.glBindVertexArray gl4 0)))

    InitGLSLProgramProtocol
    (init-glsl-program [this gl4]
      (println "init glsl")
      (let [vs (ShaderCode/create gl4 GL4/GL_VERTEX_SHADER (.getClass this) shaders-root nil "hello-triangle" "vert" nil true)
            fs (ShaderCode/create gl4 GL4/GL_FRAGMENT_SHADER (.getClass this) shaders-root nil "hello-triangle" "frag" nil true)]
        (comment (println (.dumpShaderSource vs (java.io.PrintStream. System/out))))
        (println vs)
        (println fs)
        (let [shader-program (ShaderProgram.)]
          (.add shader-program vs)
          (.add shader-program fs)
          (.init shader-program gl4)
          (reset! program-name (.program shader-program))
          (println "glsl program: " @program-name)
          (.glBindAttribLocation gl4 @program-name attr-position "position")
          (.glBindAttribLocation gl4 @program-name attr-color "color")
          (.glBindFragDataLocation gl4 @program-name attr-frag-color "outputColor")
          (.link shader-program gl4 System/out)
          (reset! mv-matrix-ul 
                  (.glGetUniformLocation gl4 @program-name "modelToClipMatrix"))
          (.destroy vs gl4)
          (.destroy fs gl4))))))

(def window-width 1024)
(def window-height 768)

(def display  (create-display "john"))
(def screen  (create-screen display 0))
(def profile  (get-gl-profile GLProfile/GL4))
(def caps (get-gl-capabilities profile)) 
(def window  (create-window screen caps))
(.setSize window window-width window-height)
(.setTitle window "john")
(.addGLEventListener window HelloTriangle)
(.setVisible window true)
(.start (Animator. window))

(defn -main [& args]
  (comment(start)))
