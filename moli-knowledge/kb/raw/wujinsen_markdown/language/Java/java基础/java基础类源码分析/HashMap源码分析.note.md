/* *

- * T h e d e f a u l t i n i t i a l c a p a c i t y - M U S T b e a p o w e r o f t w o .

- * /


static ﬁnal int D E F A U L T _IN I T I A L _C A P A C I T Y = 1 << 4; // a k a 1 6

static ﬁnal int M A X I M U M _C A P A C I T Y = 1 << 30; // 默 认 负 载 因 ⼦ static ﬁnal ﬂoat D E F A U L T _L O A D _F A C T O R = 0.75f;

- static ﬁnal int T R E E I F Y _T H R E S H O L D = 8;

- static ﬁnal int U N T R E E I F Y _T H R E S H O L D = 6;


static ﬁnal int M I N _T R E E I F Y _C A P A C I T Y = 64; /**

基础hash⼆进制节点

**/ static class Node<K,V> implements Map.Entry<K,V> {

ﬁnal int hash; ﬁnal K key; V value; Node<K,V> next;

Node(int hash, K key, V value, Node<K,V> next) { this.hash = hash; this.key = key; this.value = value; this.next = next;

}

public ﬁnal K getKey() { return key; } public ﬁnal V getValue() { return value; } public ﬁnal String toString() { return key + "=" + value; }

public ﬁnal int hashCode() {

return Objects.h a s h C o d e (key) ^ Objects.h a s h C o d e (value); }

public ﬁnal V setValue(V newValue) { V oldValue = value; value = newValue; return oldValue;

}

public ﬁnal boolean equals(Object o) { if (o == this) return true;

if (o instanceof Map.Entry) { Map.Entry<?,?> e = (Map.Entry<?,?>)o; if (Objects.e q u a l s (key, e.getKey()) &&

Objects.e q u a l s (value, e.getValue())) return true;

} return false;

} }

/*

- * C o p y r i g h t ( c ) 1 9 9 7 , 2 0 1 3 , O r a c l e a n d /o r i t s a ﬃ l i a t e s . A l l r i g h t s r e s e r v e d .

- * O R A C L E P R O P R I E T A R Y /C O N F I D E N T I A L . U s e i s s u b j e c t t o l i c e n s e t e r m s .


*

package java.util; import java.io.IOException; import java.io.InvalidObjectException; import java.io.Serializable; import java.lang.reﬂect.ParameterizedType; import java.lang.reﬂect.Type; import java.util.function.BiConsumer; import java.util.function.BiFunction; import java.util.function.Consumer; import java.util.function.Function;

/* *

- * H a s h t a b l e b a s e d i m p l e m e n t a t i o n o f t h e < t t > M a p < /t t > i n t e r f a c e . T h i s

- * i m p l e m e n t a t i o n p r o v i d e s a l l o f t h e o p t i o n a l m a p o p e r at i o n s , a n d p e r m i t s

- * < t t > n u l l < /t t > v a l u e s a n d t h e < t t > n u l l < /t t > k e y . ( T h e < t t > H a s h M a p < /t t >

- * c l a s s i s r o u g h l y e q u i v a l e n t t o < t t > H a s h t a b l e < /t t > , e x c e p t t h a t i t i s

- * u n s y n c h r o n i z e d a n d p e r m i t s n u l l s . ) T h i s c l a s s m a k e s n o g u ar an t e e s a s t o

- * t h e o r d e r o f t h e m a p ; i n p a r t i c u l ar , i t d o e s n o t g u ar an t e e t h a t t h e o r d e r

- * w i l l r e m a i n c o n s t a n t o v e r t i m e .

*

- * < p > T h i s i m p l e m e n t a t i o n p r o v i d e s c o n s t a n t - t i m e p e r f o r m an c e f o r t h e b a s i c

- * o p e r a t i o n s ( < t t > g e t < /t t > a n d < t t > p u t < /t t > ) , a s s u m i n g t h e h a s h f u n c t i o n

- * d i s p e r s e s t h e e l e m e n t s p r o p e r l y a m o n g t h e b u c k e t s . I t e r at i o n o v e r

- * c o l l e c t i o n v i e w s r e q u i r e s t i m e p r o p o r t i o n a l t o t h e " c a p a c i t y " o f t h e

- * < t t > H a s h M a p < /t t > i n s t a n c e ( t h e n u m b e r o f b u c k e t s ) p l u s i t s s i z e ( t h e n u m b e r

- * o f k e y - v a l u e m a p p i n g s ) . T h u s , i t ' s v e r y i m p o r t an t n o t t o s e t t h e i n i t i a l

- * c a p a c i t y t o o h i g h ( o r t h e l o a d f ac t o r t o o l o w ) i f i t e r at i o n p e r f o r m an c e i s

- * i m p o r t a n t .

*

- * < p > A n i n s t a n c e o f < t t > H a s h M a p < /t t > h a s t w o p a r a m e t e r s t h a t a ﬀ e c t i t s

- * p e r f o r m a n c e : < i > i n i t i a l c a p a c i t y < /i > a n d < i > l o a d f a c t o r < /i > . T h e

- * < i > c a p a c i t y < /i > i s t h e n u m b e r o f b u c k e t s i n t h e h as h t a b l e , a n d t h e i n i t i a l

- * c a p a c i t y i s s i m p l y t h e c a p a c i t y a t t h e t i m e t h e h a s h t a b l e i s c r e a t e d . T h e


- * < i > l o a d f a c t o r < /i > i s a m e a s u r e o f h o w f u l l t h e h a s h t a b l e i s a l l o w e d t o

- * g e t b e f o r e i t s c a p a c i t y i s a u t o m at i c a l l y i n c r e a s e d . W h e n t h e n u m b e r o f

- * e n t r i e s i n t h e h a s h t a b l e e x c e e d s t h e p r o d u c t o f t h e l o a d f ac t o r a n d t h e

- * c u r r e n t c a p a c i t y , t h e h a s h t a b l e i s < i > r e h a s h e d < /i > ( t h a t i s , i n t e r n a l d a t a

- * s t r u c t u r e s a r e r e b u i l t ) s o t h a t t h e h a s h t a b l e h a s a p p r o x i m a t e l y t w i c e t h e

- * n u m b e r o f b u c k e t s .

*

- * < p > A s a g e n e r a l r u l e , t h e d e f a u l t l o a d f a c t o r ( . 7 5 ) o ﬀ e r s a g o o d

- * t r a d e o ﬀ b e t w e e n t i m e a n d s p a c e c o s t s . H i g h e r v a l u e s d e c r e a s e t h e

- * s p a c e o v e r h e a d b u t i n c r e a s e t h e l o o k u p c o s t ( r e ﬂ e c t e d i n m o s t o f

- * t h e o p e r a t i o n s o f t h e < t t > H a s h M a p < /t t > c l a s s , i n c l u d i n g

- * < t t > g e t < /t t > a n d < t t > p u t < /t t > ) . T h e e x p e c t e d n u m b e r o f e n t r i e s i n

- * t h e m a p a n d i t s l o a d f a c t o r s h o u l d b e t a k e n i n t o a c c o u nt w h e n

- * s e t t i n g i t s i n i t i a l c a p a c i t y , s o a s t o m i n i m i z e t h e n u m b e r o f

- * r e h a s h o p e r a t i o n s . I f t h e i n i t i a l c a p a c i t y i s g r e a t e r t h a n t h e

- * m a x i m u m n u m b e r o f e n t r i e s d i v i d e d b y t h e l o a d f ac t o r , n o r e h a s h

- * o p e r a t i o n s w i l l e v e r o c c u r .

*

- * < p > I f m a n y m a p p i n g s a r e t o b e s t o r e d i n a < t t > H a s h M a p < /t t >

- * i n s t a n c e , c r e a t i n g i t w i t h a s u ﬃ c i e n t l y l a r g e c a p a c i t y w i l l a l l o w

- * t h e m a p p i n g s t o b e s t o r e d m o r e e ﬃ c i e n t l y t h a n l e t t i n g i t p e r f o r m

- * a u t o m a t i c r e h a s h i n g a s n e e d e d t o g r o w t h e t a b l e . N o t e t h a t u s i n g

- * m a n y k e y s w i t h t h e s a m e { @ c o d e h a s h C o d e ( ) } i s a s u r e w a y t o s l o w

- * d o w n p e r f o r m a n c e o f a n y h a s h t a b l e . T o a m e l i o r at e i m p a c t , w h e n k e y s

- * a r e { @ l i n k C o m p a r a b l e } , t h i s c l a s s m a y u s e c o m p a r i s o n o r d e r a m o n g

- * k e y s t o h e l p b r e a k t i e s .

*

- * < p > < s t r o n g > N o t e t h a t t h i s i m p l e m e n t a t i o n i s n o t s y n c h r o n i z e d . < /s t r o n g >

- * I f m u l t i p l e t h r e a d s a c c e s s a h a s h m a p c o n c u r r e n t l y , a n d a t l e a s t o n e o f

- * t h e t h r e a d s m o d i ﬁ e s t h e m a p s t r u c t u r a l l y , i t < i > m u s t < /i > b e

- * s y n c h r o n i z e d e x t e r n a l l y . ( A s t r u c t u r a l m o d i ﬁ c a t i o n i s a n y o p e r at i o n

- * t h a t a d d s o r d e l e t e s o n e o r m o r e m a p p i n g s ; m e r e l y c h a n g i n g t h e v a l u e

- * a s s o c i a t e d w i t h a k e y t h a t a n i n s t an c e a l r e a d y c o n t a i n s i s n o t a

- * s t r u c t u r a l m o d i ﬁ c a t i o n . ) T h i s i s t y p i c a l l y a c c o m p l i s h e d b y

- * s y n c h r o n i z i n g o n s o m e o b j e c t t h a t n a t u r a l l y e n c a p s u l a t e s t h e m a p .

*

- * I f n o s u c h o b j e c t e x i s t s , t h e m a p s h o u l d b e " w r a p p e d " u s i n g t h e


- * { @ l i n k C o l l e c t i o n s # s y n c h r o n i z e d M a p C o l l e c t i o n s . s y n c h r o n i z e d M a p }

- * m e t h o d . T h i s i s b e s t d o n e a t c r e a t i o n t i m e , t o p r e v e n t a c c i d e n t a l

- * u n s y n c h r o n i z e d a c c e s s t o t h e m a p : < p r e >

- * M a p m = C o l l e c t i o n s . s y n c h r o n i z e d M a p ( n e w H as h M ap ( . . . ) ) ; < /p r e >

*

- * < p > T h e i t e r a t o r s r e t u r n e d b y a l l o f t h i s c l a s s ' s " c o l l e c t i o n v i e w m e t h o d s "

- * a r e < i > f a i l - f a s t < /i > : i f t h e m a p i s s t r u c t u r a l l y m o d i ﬁ e d a t a n y t i m e a f t e r

- * t h e i t e r a t o r i s c r e a t e d , i n a n y w a y e x c e p t t h r o u g h t h e i t e r at o r ' s o w n

- * < t t > r e m o v e < /t t > m e t h o d , t h e i t e r a t o r w i l l t h r o w a

- * { @ l i n k C o n c u r r e n t M o d i ﬁ c a t i o n E x c e p t i o n } . T h u s , i n t h e f ac e o f c o n c u r r e n t

- * m o d i ﬁ c a t i o n , t h e i t e r a t o r f a i l s q u i c k l y a n d c l e a n l y , r at h e r t h a n r i s k i n g

- * a r b i t r a r y , n o n - d e t e r m i n i s t i c b e h a v i o r a t a n u nd e t e r m i n e d t i m e i n t h e

- * f u t u r e .

*

- * < p > N o t e t h a t t h e f a i l - f a s t b e h a v i o r o f a n i t e r at o r c a n n o t b e g u ar an t e e d

- * a s i t i s , g e n e r a l l y s p e a k i n g , i m p o s s i b l e t o m a k e a n y h a r d g u ar an t e e s i n t h e

- * p r e s e n c e o f u n s y n c h r o n i z e d c o n c u r r e n t m o d i ﬁ c a t i o n . F a i l - f as t i t e r a t o r s

- * t h r o w < t t > C o n c u r r e n t M o d i ﬁ c a t i o n E x c e p t i o n < /t t > o n a b e s t - e ﬀ o r t b a s i s .

- * T h e r e f o r e , i t w o u l d b e w r o n g t o w r i t e a p r o g r am t h a t d e p e n d e d o n t h i s

- * e x c e p t i o n f o r i t s c o r r e c t n e s s : < i > t h e f a i l - f a s t b e h a v i o r o f i t e r a t o r s

- * s h o u l d b e u s e d o n l y t o d e t e c t b u g s . < /i >

*

- * < p > T h i s c l a s s i s a m e m b e r o f t h e

- * < a h r e f = " { @ d o c R o o t } /. . /t e c h n o t e s /g u i d e s /c o l l e c t i o n s /i n d e x . h t m l " >

- * J a v a C o l l e c t i o n s F r a m e w o r k < /a > .

*

- * @ p a r a m < K > t h e t y p e o f k e y s m a i n t a i n e d b y t h i s m a p

- * @ p a r a m < V > t h e t y p e o f m a p p e d v a l u e s

*

- * @ a u t h o r D o u g L e a

- * @ a u t h o r J o s h B l o c h

- * @ a u t h o r A r t h u r v a n H o ﬀ

- * @ a u t h o r N e a l G a f t e r

- * @ s e e O b j e c t # h a s h C o d e ( )

- * @ s e e C o l l e c t i o n

- * @ s e e M a p

- * @ s e e T r e e M a p


- * @ s e e H a s h t a b l e

- * @ s i n c e 1 . 2

- * /


public class HashMap<K,V> extends AbstractMap<K,V> implements Map<K,V>, Cloneable, Serializable {

private static ﬁnal long s e r i a l V e r s i o n U I D = 362498820763181265L;

/*

- * I m p l e m e n t a t i o n n o t e s .

*

- * T h i s m a p u s u a l l y a c t s a s a b i nn e d ( b u c k e t e d ) h a s h t a b l e , b u t

- * w h e n b i n s g e t t o o l a r g e , t h e y a r e t r an s f o r m e d i n t o b i n s o f

- * T r e e N o d e s , e a c h s t r u c t u r e d s i m i l a r l y t o t h o s e i n

- * j a v a . u t i l . T r e e M a p . M o s t m e t h o d s t r y t o u s e n o r m al b i n s , b u t

- * r e l a y t o T r e e N o d e m e t h o d s w h e n a p p l i c a b l e ( s i m p l y b y c h e c k i n g

- * i n s t a n c e o f a n o d e ) . B i n s o f T r e e N o d e s m a y b e t r av e r s e d a n d

- * u s e d l i k e a n y o t h e r s , b u t a d d i t i o n a l l y s u p p o r t f as t e r l o o k u p

- * w h e n o v e r p o p u l a t e d . H o w e v e r , s i n c e t h e v a s t m a j o r i t y o f b i n s i n

- * n o r m a l u s e a r e n o t o v e r p o p u l a t e d , c h e c k i n g f o r e x i s t e n c e o f

- * t r e e b i n s m a y b e d e l a y e d i n t h e c o u r s e o f t a b l e m e t h o d s .

*

- * T r e e b i n s ( i . e . , b i n s w h o s e e l e m e n t s a r e a l l T r e e N o d e s ) a r e

- * o r d e r e d p r i m a r i l y b y h a s h C o d e , b u t i n t h e c a s e o f t i e s , i f t w o

- * e l e m e n t s a r e o f t h e s a m e " c l a s s C i m p l e m e n t s C o m p a r ab l e < C > " ,

- * t y p e t h e n t h e i r c o m p a r e T o m e t h o d i s u s e d f o r o r d e r i n g . ( W e

- * c o n s e r v a t i v e l y c h e c k g e n e r i c t y p e s v i a r e ﬂ e c t i o n t o v a l i d a t e

- * t h i s - - s e e m e t h o d c o m p a r a b l e C l a s s F o r ) . T h e a d d e d c o m p l e x i t y

- * o f t r e e b i n s i s w o r t h w h i l e i n p r o v i d i n g w o r s t - c a s e O ( l o g n )

- * o p e r a t i o n s w h e n k e y s e i t h e r h a v e d i s t i n c t h a s h e s o r a r e

- * o r d e r a b l e , T h u s , p e r f o r m a n c e d e g r ad e s g r ac e f u l l y u nd e r

- * a c c i d e n t a l o r m a l i c i o u s u s a g e s i n w h i c h h a s h C o d e ( ) m e t h o d s

- * r e t u r n v a l u e s t h a t a r e p o o r l y d i s t r i b u t e d , a s w e l l a s t h o s e i n

- * w h i c h m a n y k e y s s h a r e a h a s h C o d e , s o l o n g a s t h e y a r e a l s o

- * C o m p a r a b l e . ( I f n e i t h e r o f t h e s e a p p l y , w e m a y w as t e a b o u t a

- * f a c t o r o f t w o i n t i m e a n d s p ac e c o m p a r e d t o t a k i n g n o

- * p r e c a u t i o n s . B u t t h e o n l y k n o w n c a s e s s t e m f r o m p o o r u s e r


- * p r o g r a m m i n g p r a c t i c e s t h a t a r e a l r e a d y s o s l o w t h a t t h i s m a k e s

- * l i t t l e d i ﬀ e r e n c e . )

*

- * B e c a u s e T r e e N o d e s a r e a b o u t t w i c e t h e s i z e o f r e g u l a r n o d e s , w e

- * u s e t h e m o n l y w h e n b i n s c o n t ai n e n o u g h n o d e s t o w ar r a n t u s e

- * ( s e e T R E E I F Y _T H R E S H O L D ) . A nd w h e n t h e y b e c o m e t o o s m al l ( d u e t o

- * r e m o v a l o r r e s i z i n g ) t h e y a r e c o n v e r t e d b a c k t o p l a i n b i n s . I n

- * u s a g e s w i t h w e l l - d i s t r i b u t e d u s e r h a s h C o d e s , t r e e b i n s a r e

- * r a r e l y u s e d . I d e a l l y , u n d e r r a n d o m h a s h C o d e s , t h e f r e q u e n c y o f

- * n o d e s i n b i n s f o l l o w s a P o i s s o n d i s t r i b u t i o n

- * ( h t t p : //e n . w i k i p e d i a . o r g /w i k i / P o i s s o n _d i s t r i b u t i o n ) w i t h a

- * p a r a m e t e r o f a b o u t 0 . 5 o n a v e r ag e f o r t h e d e f au l t r e s i z i n g

- * t h r e s h o l d o f 0 . 7 5 , a l t h o u g h w i t h a l a r g e v a r i a n c e b e c a u s e o f

- * r e s i z i n g g r a n u l a r i t y . I g n o r i n g v a r i a n c e , t h e e x p e c t e d

- * o c c u r r e n c e s o f l i s t s i z e k a r e ( e x p ( - 0 . 5 ) * p o w ( 0 . 5 , k ) /

- * f a c t o r i a l ( k ) ) . T h e ﬁ r s t v a l u e s a r e :

*

- * 0 : 0 . 6 0 6 5 3 0 6 6

- * 1 : 0 . 3 0 3 2 6 5 3 3

- * 2 : 0 . 0 7 5 8 1 6 3 3

- * 3 : 0 . 0 1 2 6 3 6 0 6

- * 4 : 0 . 0 0 1 5 7 9 5 2

- * 5 : 0 . 0 0 0 1 5 7 9 5

- * 6 : 0 . 0 0 0 0 1 3 1 6

- * 7 : 0 . 0 0 0 0 0 0 9 4

- * 8 : 0 . 0 0 0 0 0 0 0 6

- * m o r e : l e s s t h a n 1 i n t e n m i l l i o n

*

- * T h e r o o t o f a t r e e b i n i s n o r m a l l y i t s ﬁ r s t n o d e . H o w e v e r ,

- * s o m e t i m e s ( c u r r e n t l y o n l y u p o n I t e r at o r . r e m o v e ) , t h e r o o t m i g h t

- * b e e l s e w h e r e , b u t c a n b e r e c o v e r e d f o l l o w i n g p a r e n t l i n k s

- * ( m e t h o d T r e e N o d e . r o o t ( ) ) .

*

- * A l l a p p l i c a b l e i n t e r n a l m e t h o d s a c c e p t a h a s h c o d e a s a n

- * a r g u m e n t ( a s n o r m a l l y s u p p l i e d f r o m a p u b l i c m e t h o d ) , a l l o w i n g

- * t h e m t o c a l l e a c h o t h e r w i t h o u t r e c o m p u t i n g u s e r h a s h C o d e s .

- * M o s t i n t e r n a l m e t h o d s a l s o a c c e p t a " t a b " a r g u m e n t , t h a t i s


- * n o r m a l l y t h e c u r r e n t t a b l e , b u t m a y b e a n e w o r o l d o n e w h e n

- * r e s i z i n g o r c o n v e r t i n g .

*

- * W h e n b i n l i s t s a r e t r e e i ﬁ e d , s p l i t , o r u nt r e e i ﬁ e d , w e k e e p

- * t h e m i n t h e s a m e r e l a t i v e a c c e s s /t r av e r s a l o r d e r ( i . e . , ﬁ e l d

- * N o d e . n e x t ) t o b e t t e r p r e s e r v e l o c a l i t y , a n d t o s l i g h t l y

- * s i m p l i f y h a n d l i n g o f s p l i t s a n d t r av e r s a l s t h a t i n v o k e

- * i t e r a t o r . r e m o v e . W h e n u s i n g c o m p a r at o r s o n i n s e r t i o n , t o k e e p a

- * t o t a l o r d e r i n g ( o r a s c l o s e a s i s r e q u i r e d h e r e ) a c r o s s

- * r e b a l a n c i n g s , w e c o m p a r e c l a s s e s a n d i d e n t i t y H as h C o d e s a s

- * t i e - b r e a k e r s .

*

- * T h e u s e a n d t r a n s i t i o n s a m o n g p l a i n v s t r e e m o d e s i s

- * c o m p l i c a t e d b y t h e e x i s t e n c e o f s u b c l a s s L i n k e d H as h M ap . S e e

- * b e l o w f o r h o o k m e t h o d s d e ﬁ n e d t o b e i n v o k e d u p o n i n s e r t i o n ,

- * r e m o v a l a n d a c c e s s t h a t a l l o w L i n k e d H as h M ap i n t e r na l s t o

- * o t h e r w i s e r e m a i n i n d e p e n d e nt o f t h e s e m e c h a n i c s . ( T h i s a l s o

- * r e q u i r e s t h a t a m a p i n s t a n c e b e p a s s e d t o s o m e u t i l i t y m e t h o d s

- * t h a t m a y c r e a t e n e w n o d e s . )

*

- * T h e c o n c u r r e n t - p r o g r a m m i n g - l i k e S S A - b a s e d c o d i n g s t y l e h e l p s

- * a v o i d a l i a s i n g e r r o r s a m i d a l l o f t h e t w i s t y p o i n t e r o p e r at i o n s .

- * /


/* *

- * T h e d e f a u l t i n i t i a l c a p a c i t y - M U S T b e a p o w e r o f t w o .

- * /


static ﬁnal int D E F A U L T _IN I T I A L _C A P A C I T Y = 1 << 4; // a k a 1 6

/* *

- * T h e m a x i m u m c a p a c i t y , u s e d i f a h i g h e r v a l u e i s i m p l i c i t l y s p e c i ﬁ e d

- * b y e i t h e r o f t h e c o n s t r u c t o r s w i t h a r g u m e n t s .

- * M U S T b e a p o w e r o f t w o < = 1 < < 3 0 .

- * /


static ﬁnal int M A X I M U M _C A P A C I T Y = 1 << 30;

/* *

- * T h e l o a d f a c t o r u s e d w h e n n o ne s p e c i ﬁ e d i n c o n s t r u c t o r .

- * /


static ﬁnal ﬂoat D E F A U L T _L O A D _F A C T O R = 0.75f;

/* *

- * T h e b i n c o u n t t h r e s h o l d f o r u s i n g a t r e e r at h e r t h a n l i s t f o r a

- * b i n . B i n s a r e c o n v e r t e d t o t r e e s w h e n a d d i n g a n e l e m e n t t o a

- * b i n w i t h a t l e a s t t h i s m a n y n o d e s . T h e v a l u e m u s t b e g r e a t e r

- * t h a n 2 a n d s h o u l d b e a t l e a s t 8 t o m e s h w i t h a s s u m p t i o n s i n

- * t r e e r e m o v a l a b o u t c o n v e r s i o n b a c k t o p l a i n b i n s u p o n

- * s h r i n k a g e .

- * /


- static ﬁnal int T R E E I F Y _T H R E S H O L D = 8;

/* *

- * T h e b i n c o u n t t h r e s h o l d f o r u nt r e e i f y i n g a ( s p l i t ) b i n d u r i n g a

- * r e s i z e o p e r a t i o n . S h o u l d b e l e s s t h a n T R E E I F Y _T H R E S H O L D , a n d a t

- * m o s t 6 t o m e s h w i t h s h r i n k a g e d e t e c t i o n u nd e r r e m o v a l .

- * /


- static ﬁnal int U N T R E E I F Y _T H R E S H O L D = 6;


/* *

- * T h e s m a l l e s t t a b l e c a p a c i t y f o r w h i c h b i n s m a y b e t r e e i ﬁ e d .

- * ( O t h e r w i s e t h e t a b l e i s r e s i z e d i f t o o m a n y n o d e s i n a b i n . )

- * S h o u l d b e a t l e a s t 4 * T R E E I F Y _T H R E S H O L D t o a v o i d c o n ﬂ i c t s

- * b e t w e e n r e s i z i n g a n d t r e e i ﬁ c a t i o n t h r e s h o l d s .

- * /


static ﬁnal int M I N _T R E E I F Y _C A P A C I T Y = 64;

/* *

- * B a s i c h a s h b i n n o d e , u s e d f o r m o s t e n t r i e s . ( S e e b e l o w f o r

- * T r e e N o d e s u b c l a s s , a n d i n L i n k e d H as h M ap f o r i t s E nt r y s u b c l a s s . )

- * /


static class Node<K,V> implements Map.Entry<K,V> { ﬁnal int hash; ﬁnal K key; V value;

Node<K,V> next;

Node(int hash, K key, V value, Node<K,V> next) { this.hash = hash; this.key = key; this.value = value; this.next = next;

}

public ﬁnal K getKey() { return key; } public ﬁnal V getValue() { return value; } public ﬁnal String toString() { return key + "=" + value; }

public ﬁnal int hashCode() {

return Objects.h a s h C o d e (key) ^ Objects.h a s h C o d e (value); }

public ﬁnal V setValue(V newValue) { V oldValue = value; value = newValue; return oldValue;

}

public ﬁnal boolean equals(Object o) { if (o == this) return true;

if (o instanceof Map.Entry) { Map.Entry<?,?> e = (Map.Entry<?,?>)o; if (Objects.e q u a l s (key, e.getKey()) &&

Objects.e q u a l s (value, e.getValue())) return true;

} return false;

} }

# /* - - - - - - - - - - - - - - - - S t a t i c u t i l i t i e s - - - - - - - - - - - - - - * /

/* *

- * C o m p u t e s k e y . h a s h C o d e ( ) a n d s p r e a d s ( X O R s ) h i g h e r b i t s o f h a s h

- * t o l o w e r . B e c a u s e t h e t a b l e u s e s p o w e r - o f - t w o m a s k i n g , s e t s o f

- * h a s h e s t h a t v a r y o n l y i n b i t s a b o v e t h e c u r r e n t m a s k w i l l

- * a l w a y s c o l l i d e . ( A m o n g k n o w n e x a m p l e s a r e s e t s o f F l o a t k e y s

- * h o l d i n g c o n s e c u t i v e w h o l e n u m b e r s i n s m al l t a b l e s . ) S o w e

- * a p p l y a t r a n s f o r m t h a t s p r e a d s t h e i m p a c t o f h i g h e r b i t s

- * d o w n w a r d . T h e r e i s a t r a d e o ﬀ b e t w e e n s p e e d , u t i l i t y , a n d

- * q u a l i t y o f b i t - s p r e a d i n g . B e c a u s e m a n y c o m m o n s e t s o f h a s h e s

- * a r e a l r e a d y r e a s o n a b l y d i s t r i b u t e d ( s o d o n ' t b e n e ﬁ t f r o m

- * s p r e a d i n g ) , a n d b e c a u s e w e u s e t r e e s t o h a n d l e l a r g e s e t s o f

- * c o l l i s i o n s i n b i n s , w e j u s t X O R s o m e s h i f t e d b i t s i n t h e

- * c h e a p e s t p o s s i b l e w a y t o r e d u c e s y s t e m a t i c l o s s a g e , a s w e l l a s

- * t o i n c o r p o r a t e i m p a c t o f t h e h i g h e s t b i t s t h a t w o u l d o t h e r w i s e

- * n e v e r b e u s e d i n i n d e x c a l c u l at i o n s b e c a u s e o f t a b l e b o u nd s .

- * /


static ﬁnal int hash(Object key) { int h; return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);

}

/* *

- * R e t u r n s x ' s C l a s s i f i t i s o f t h e f o r m " c l a s s C i m p l e m e n t s

- * C o m p a r a b l e < C > " , e l s e n u l l .

- * /


static Class<?> comparableClassFor(Object x) {

if (x instanceof Comparable) { Class<?> c; Type[] ts, as; Type t; ParameterizedType p; if ((c = x.getClass()) == String.class) // b y p a s s c h e c k s

return c; if ((ts = c.getGenericInterfaces()) != null) { for (int i = 0; i < ts.length; ++i) {

if (((t = ts[i]) instanceof ParameterizedType) && ((p = (ParameterizedType)t).getRawType() ==

Comparable.class) && (as = p.getActualTypeArguments()) != null &&

as.length == 1 && as[0] == c) // t y p e a r g i s c return c;

} }

} return null;

}

/* *

- * R e t u r n s k . c o m p a r e T o ( x ) i f x m a t c h e s k c ( k ' s s c r e e n e d c o m p a r ab l e

- * c l a s s ) , e l s e 0 .

- * /


@SuppressWarnings({"rawtypes","unchecked"}) // f o r c a s t t o C o m p a r a b l e static int compareComparables(Class<?> kc, Object k, Object x) {

return (x == null || x.getClass() != kc ? 0 :

((Comparable)k).compareTo(x)); }

/* *

- * R e t u r n s a p o w e r o f t w o s i z e f o r t h e g i v e n t a r g e t c a p a c i t y .

- * /


static ﬁnal int tableSizeFor(int cap) { int n = cap - 1;

- n |= n >>> 1;

- n |= n >>> 2; n |= n >>> 4; n |= n >>> 8; n |= n >>> 16; return (n < 0) ? 1 : (n >= M A X I M U M _C A P A C I T Y ) ? M A X I M U M _C A P A C I T Y : n + 1;


}

/* - - - - - - - - - - - - - - - - F i e l d s - - - - - - - - - - - - - - * /

/* *

- * T h e t a b l e , i n i t i a l i z e d o n ﬁ r s t u s e , a n d r e s i z e d a s

- * n e c e s s a r y . W h e n a l l o c a t e d , l e n g t h i s a l w ay s a p o w e r o f t w o .

- * ( W e a l s o t o l e r a t e l e n g t h z e r o i n s o m e o p e r at i o n s t o a l l o w


- * b o o t s t r a p p i n g m e c h a n i c s t h a t a r e c u r r e n t l y n o t n e e d e d . )

- * /


transient Node<K,V>[] table;

/* *

- * H o l d s c a c h e d e n t r y S e t ( ) . N o t e t h a t A b s t r a c t M a p ﬁ e l d s a r e u s e d

- * f o r k e y S e t ( ) a n d v a l u e s ( ) .

- * /


transient Set<Map.Entry<K,V>> entrySet;

/* *

- * T h e n u m b e r o f k e y - v a l u e m a p p i n g s c o n t a i n e d i n t h i s m a p .

- * /


transient int size;

/* *

- * T h e n u m b e r o f t i m e s t h i s H a s h M a p h a s b e e n s t r u c t u r a l l y m o d i ﬁ e d

- * S t r u c t u r a l m o d i ﬁ c a t i o n s a r e t h o s e t h a t c h a n g e t h e n u m b e r o f m a p p i n g s i n

- * t h e H a s h M a p o r o t h e r w i s e m o d i f y i t s i n t e r na l s t r u c t u r e ( e . g . ,

- * r e h a s h ) . T h i s ﬁ e l d i s u s e d t o m a k e i t e r at o r s o n C o l l e c t i o n - v i e w s o f

- * t h e H a s h M a p f a i l - f a s t . ( S e e C o n c u r r e n t M o d i ﬁ c a t i o n E x c e p t i o n ) .

- * /


transient int modCount;

/* *

- * T h e n e x t s i z e v a l u e a t w h i c h t o r e s i z e ( c a p a c i t y * l o a d f ac t o r ) .

*

- * @ s e r i a l

- * /


// ( T h e j a v a d o c d e s c r i p t i o n i s t r u e u p o n s e r i a l i z a t i o n . // A d d i t i o n a l l y , i f t h e t a b l e a r r a y h a s n o t b e e n a l l o c a t e d , t h i s // ﬁ e l d h o l d s t h e i n i t i a l a r r a y c a p a c i t y , o r z e r o s i g n i f y i n g // D E F A U L T _I N I T I A L _C A P A C I T Y . )

int threshold;

/* *

- * T h e l o a d f a c t o r f o r t h e h a s h t a b l e .


- *
- * @ s e r i a l

- * /


ﬁnal ﬂoat loadFactor;

/* - - - - - - - - - - - - - - - - P u b l i c o p e r a t i o n s - - - - - - - - - - - - - - * /

/* *

- * C o n s t r u c t s a n e m p t y < t t > H a s h M a p < /t t > w i t h t h e s p e c i ﬁ e d i n i t i a l

- * c a p a c i t y a n d l o a d f a c t o r .

*

- * @ p a r a m i n i t i a l C a p a c i t y t h e i n i t i a l c a p a c i t y

- * @ p a r a m l o a d F a c t o r t h e l o a d f a c t o r

- * @ t h r o w s I l l e g a l A r g u m e n t E x c e p t i o n i f t h e i n i t i a l c a p a c i t y i s n e g a t i v e

- * o r t h e l o a d f a c t o r i s n o n p o s i t i v e

- * /


public HashMap(int initialCapacity, ﬂoat loadFactor) { if (initialCapacity < 0) throw new IllegalArgumentException("Illegal initial capacity: " +

initialCapacity); if (initialCapacity > M A X I M U M _C A P A C I T Y )

initialCapacity = M A X I M U M _C A P A C I T Y ; if (loadFactor <= 0 || Float.i s N a N (loadFactor))

throw new IllegalArgumentException("Illegal load factor: " +

loadFactor); this.loadFactor = loadFactor; this.threshold = t a b l e S i z e F o r (initialCapacity);

}

/* *

- * C o n s t r u c t s a n e m p t y < t t > H a s h M a p < /t t > w i t h t h e s p e c i ﬁ e d i n i t i a l

- * c a p a c i t y a n d t h e d e f a u l t l o a d f ac t o r ( 0 . 7 5 ) .

*

- * @ p a r a m i n i t i a l C a p a c i t y t h e i n i t i a l c a p a c i t y .

- * @ t h r o w s I l l e g a l A r g u m e n t E x c e p t i o n i f t h e i n i t i a l c a p a c i t y i s n e g a t i v e .

- * /


public HashMap(int initialCapacity) {

this(initialCapacity, D E F A U L T _L O A D _F A C T O R ); }

/* *

- * C o n s t r u c t s a n e m p t y < t t > H a s h M a p < /t t > w i t h t h e d e f a u l t i n i t i a l c a p a c i t y

- * ( 1 6 ) a n d t h e d e f a u l t l o a d f a c t o r ( 0 . 7 5 ) .

- * /


public HashMap() {

this.loadFactor = D E F A U L T _L O A D _F A C T O R ; // a l l o t h e r ﬁ e l d s d e f a u l t e d }

/* *

- * C o n s t r u c t s a n e w < t t > H a s h M a p < /t t > w i t h t h e s a m e m a p p i n g s a s t h e

- * s p e c i ﬁ e d < t t > M a p < /t t > . T h e < t t > H a s h M a p < /t t > i s c r e a t e d w i t h

- * d e f a u l t l o a d f a c t o r ( 0 . 7 5 ) a n d a n i n i t i a l c a p a c i t y s u ﬃ c i e n t t o

- * h o l d t h e m a p p i n g s i n t h e s p e c i ﬁ e d < t t > M a p < /t t > .

*

- * @ p a r a m m t h e m a p w h o s e m a p p i n g s a r e t o b e p l a c e d i n t h i s m a p

- * @ t h r o w s N u l l P o i n t e r E x c e p t i o n i f t h e s p e c i ﬁ e d m a p i s n u l l

- * /


public HashMap(Map<? extends K, ? extends V> m) { this.loadFactor = D E F A U L T _L O A D _F A C T O R ; putMapEntries(m, false);

}

/* *

- * I m p l e m e n t s M a p . p u t A l l a n d M a p c o n s t r u c t o r

*

- * @ p a r a m m t h e m a p

- * @ p a r a m e v i c t f a l s e w h e n i n i t i a l l y c o n s t r u c t i n g t h i s m a p , e l s e

- * t r u e ( r e l a y e d t o m e t h o d a f t e r N o d e I n s e r t i o n ) .

- * /


ﬁnal void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) { int s = m.size(); if (s > 0) {

if (table == null) { // p r e - s i z e

ﬂoat ft = ((ﬂoat)s / loadFactor) + 1.0F;

int t = ((ft < (ﬂoat)M A X I M U M _C A P A C I T Y ) ? (int)ft : M A X I M U M _C A P A C I T Y ); if (t > threshold)

threshold = t a b l e S i z e F o r (t);

} else if (s > threshold)

resize();

for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) { K key = e.getKey(); V value = e.getValue(); putVal(h a s h (key), key, value, false, evict);

} }

}

/* *

- * R e t u r n s t h e n u m b e r o f k e y - v a l u e m a p p i n g s i n t h i s m a p .

*

- * @ r e t u r n t h e n u m b e r o f k e y - v a l u e m a p p i n g s i n t h i s m a p

- * /


public int size() { return size; }

/* *

- * R e t u r n s < t t > t r u e < /t t > i f t h i s m a p c o n t a i n s n o k e y - v a l u e m a p p i n g s .

*

- * @ r e t u r n < t t > t r u e < /t t > i f t h i s m a p c o n t a i n s n o k e y - v a l u e m a p p i n g s

- * /


public boolean isEmpty() {

return size == 0; }

/* *

- * R e t u r n s t h e v a l u e t o w h i c h t h e s p e c i ﬁ e d k e y i s m a p p e d ,

- * o r { @ c o d e n u l l } i f t h i s m a p c o n t a i n s n o m a p p i ng f o r t h e k e y .


*

- * < p > M o r e f o r m a l l y , i f t h i s m a p c o n t a i n s a m a p p i n g f r o m a k e y

- * { @ c o d e k } t o a v a l u e { @ c o d e v } s u c h t h a t { @ c o d e ( k e y = = n u l l ? k = = n u l l :

- * k e y . e q u a l s ( k ) ) } , t h e n t h i s m e t h o d r e t u r n s { @ c o d e v } ; o t h e r w i s e

- * i t r e t u r n s { @ c o d e n u l l } . ( T h e r e c a n b e a t m o s t o n e s u c h m a p p i n g . )

*

- * < p > A r e t u r n v a l u e o f { @ c o d e n u l l } d o e s n o t < i > n e c e s s a r i l y < /i >

- * i n d i c a t e t h a t t h e m a p c o n t a i n s n o m a p p i n g f o r t h e k e y ; i t ' s a l s o

- * p o s s i b l e t h a t t h e m a p e x p l i c i t l y m a p s t h e k e y t o { @ c o d e n u l l } .

- * T h e { @ l i n k # c o n t a i n s K e y c o n t a i n s K e y } o p e r a t i o n m a y b e u s e d t o

- * d i s t i n g u i s h t h e s e t w o c a s e s .

*

- * @ s e e # p u t ( O b j e c t , O b j e c t )

- * /


public V get(Object key) { Node<K,V> e; return (e = getNode(h a s h (key), key)) == null ? null : e.value;

}

/* *

- * I m p l e m e n t s M a p . g e t a n d r e l a t e d m e t h o d s

*

- * @ p a r a m h a s h h a s h f o r k e y

- * @ p a r a m k e y t h e k e y

- * @ r e t u r n t h e n o d e , o r n u l l i f n o n e

- * /


ﬁnal Node<K,V> getNode(int hash, Object key) { Node<K,V>[] tab; Node<K,V> ﬁrst, e; int n; K k; if ((tab = table) != null && (n = tab.length) > 0 &&

(ﬁrst = tab[(n - 1) & hash]) != null) { if (ﬁrst.hash == hash && // a l w a y s c h e c k ﬁ r s t n o d e

((k = ﬁrst.key) == key || (key != null && key.equals(k)))) return ﬁrst;

if ((e = ﬁrst.next) != null) { if (ﬁrst instanceof TreeNode)

return ((TreeNode<K,V>)ﬁrst).getTreeNode(hash, key); do {

if (e.hash == hash &&

((k = e.key) == key || (key != null && key.equals(k)))) return e;

} while ((e = e.next) != null); }

} return null;

}

/* *

- * R e t u r n s < t t > t r u e < /t t > i f t h i s m a p c o n t a i n s a m a p p i n g f o r t h e

- * s p e c i ﬁ e d k e y .

*

- * @ p a r a m k e y T h e k e y w h o s e p r e s e n c e i n t h i s m a p i s t o b e t e s t e d

- * @ r e t u r n < t t > t r u e < /t t > i f t h i s m a p c o n t a i n s a m a p p i n g f o r t h e s p e c i ﬁ e d

- * k e y .

- * /


public boolean containsKey(Object key) {

return getNode(h a s h (key), key) != null; }

/* *

- * A s s o c i a t e s t h e s p e c i ﬁ e d v a l u e w i t h t h e s p e c i ﬁ e d k e y i n t h i s m a p .

- * I f t h e m a p p r e v i o u s l y c o n t a i n e d a m a p p i n g f o r t h e k e y , t h e o l d

- * v a l u e i s r e p l a c e d .

*

- * @ p a r a m k e y k e y w i t h w h i c h t h e s p e c i ﬁ e d v a l u e i s t o b e a s s o c i a t e d

- * @ p a r a m v a l u e v a l u e t o b e a s s o c i a t e d w i t h t h e s p e c i ﬁ e d k e y

- * @ r e t u r n t h e p r e v i o u s v a l u e a s s o c i a t e d w i t h < t t > k e y < /t t > , o r

- * < t t > n u l l < /t t > i f t h e r e w a s n o m a p p i n g f o r < t t > k e y < /t t > .

- * ( A < t t > n u l l < /t t > r e t u r n c a n a l s o i n d i c a t e t h a t t h e m ap

- * p r e v i o u s l y a s s o c i a t e d < t t > n u l l < /t t > w i t h < t t > k e y < /t t > . )

- * /


public V put(K key, V value) {

return putVal(h a s h (key), key, value, false, true); }

# /* *

- * I m p l e m e n t s M a p . p u t a n d r e l a t e d m e t h o d s

*

- * @ p a r a m h a s h h a s h f o r k e y

- * @ p a r a m k e y t h e k e y

- * @ p a r a m v a l u e t h e v a l u e t o p u t

- * @ p a r a m o n l y I f A b s e n t i f t r u e , d o n ' t c h a n g e e x i s t i n g v a l u e

- * @ p a r a m e v i c t i f f a l s e , t h e t a b l e i s i n c r e a t i o n m o d e .

- * @ r e t u r n p r e v i o u s v a l u e , o r n u l l i f n o n e

- * /


ﬁnal V putVal(int hash, K key, V value, boolean onlyIfAbsent,

boolean evict) { Node<K,V>[] tab; Node<K,V> p; int n, i; if ((tab = table) == null || (n = tab.length) == 0)

n = (tab = resize()).length; if ((p = tab[i = (n - 1) & hash]) == null) tab[i] = newNode(hash, key, value, null);

else { Node<K,V> e; K k; if (p.hash == hash &&

((k = p.key) == key || (key != null && key.equals(k)))) e = p;

else if (p instanceof TreeNode)

e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value); else {

for (int binCount = 0; ; ++binCount) {

if ((e = p.next) == null) { p.next = newNode(hash, key, value, null); if (binCount >= T R E E I F Y _T H R E S H O L D - 1) // - 1 f o r 1 s t

treeifyBin(tab, hash); break;

} if (e.hash == hash &&

((k = e.key) == key || (key != null && key.equals(k)))) break;

p = e; }

}

if (e != null) { // e x i s t i n g m a p p i n g f o r k e y V oldValue = e.value; if (!onlyIfAbsent || oldValue == null)

e.value = value; afterNodeAccess(e); return oldValue;

} }

++modCount; if (++size > threshold)

resize(); afterNodeInsertion(evict); return null;

}

/* *

- * I n i t i a l i z e s o r d o u b l e s t a b l e s i z e . I f n u l l , a l l o c a t e s i n

- * a c c o r d w i t h i n i t i a l c a p a c i t y t a r g e t h e l d i n ﬁ e l d t h r e s h o l d .

- * O t h e r w i s e , b e c a u s e w e a r e u s i n g p o w e r - o f - t w o e x p a n s i o n , t h e

- * e l e m e n t s f r o m e a c h b i n m u s t e i t h e r s t ay a t s am e i n d e x , o r m o v e

- * w i t h a p o w e r o f t w o o ﬀ s e t i n t h e n e w t a b l e .

*

- * @ r e t u r n t h e t a b l e

- * /


ﬁnal Node<K,V>[] resize() { Node<K,V>[] oldTab = table; int oldCap = (oldTab == null) ? 0 : oldTab.length; int oldThr = threshold; int newCap, newThr = 0; if (oldCap > 0) {

if (oldCap >= M A X I M U M _C A P A C I T Y ) { threshold = Integer.M A X _V A L U E ; return oldTab;

} else if ((newCap = oldCap << 1) < M A X I M U M _C A P A C I T Y &&

oldCap >= D E F A U L T _IN I T I A L _C A P A C I T Y ) newThr = oldThr << 1; // d o u b l e t h r e s h o l d

# } else if (oldThr > 0) // i n i t i a l c a p a c i t y w a s p l a c e d i n t h r e s h o l d

newCap = oldThr;

# else { // z e r o i n i t i a l t h r e s h o l d s i g n i ﬁ e s u s i n g d e f au l t s newCap = D E F A U L T _IN I T I A L _C A P A C I T Y ; newThr = (int)(D E F A U L T _L O A D _F A C T O R * D E F A U L T _IN I T I A L _C A P A C I T Y );

} if (newThr == 0) {

ﬂoat ft = (ﬂoat)newCap * loadFactor; newThr = (newCap < M A X I M U M _C A P A C I T Y && ft < (ﬂoat)M A X I M U M _C A P A C I T Y ?

(int)ft : Integer.M A X _V A L U E );

} threshold = newThr; @SuppressWarnings({"rawtypes","unchecked"})

Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap]; table = newTab; if (oldTab != null) {

for (int j = 0; j < oldCap; ++j) { Node<K,V> e; if ((e = oldTab[j]) != null) {

oldTab[j] = null; if (e.next == null)

newTab[e.hash & (newCap - 1)] = e; else if (e instanceof TreeNode)

((TreeNode<K,V>)e).split(this, newTab, j, oldCap); else { // p r e s e r v e o r d e r

Node<K,V> loHead = null, loTail = null; Node<K,V> hiHead = null, hiTail = null; Node<K,V> next;

do { next = e.next; if ((e.hash & oldCap) == 0) {

if (loTail == null) loHead = e; else

loTail.next = e; loTail = e;

} else {

if (hiTail == null) hiHead = e; else

hiTail.next = e; hiTail = e;

} } while ((e = next) != null); if (loTail != null) {

loTail.next = null; newTab[j] = loHead;

} if (hiTail != null) {

hiTail.next = null; newTab[j + oldCap] = hiHead;

} }

} }

} return newTab;

}

/* *

- * R e p l a c e s a l l l i n k e d n o d e s i n b i n a t i n d e x f o r g i v e n h a s h u nl e s s

- * t a b l e i s t o o s m a l l , i n w h i c h c as e r e s i z e s i n s t e a d .

- * /


ﬁnal void treeifyBin(Node<K,V>[] tab, int hash) { int n, index; Node<K,V> e; if (tab == null || (n = tab.length) < M I N _T R E E I F Y _C A P A C I T Y )

resize();

else if ((e = tab[index = (n - 1) & hash]) != null) { TreeNode<K,V> hd = null, tl = null; do {

TreeNode<K,V> p = replacementTreeNode(e, null); if (tl == null)

hd = p; else {

p.prev = tl; tl.next = p;

} tl = p;

} while ((e = e.next) != null); if ((tab[index] = hd) != null)

hd.treeify(tab); }

}

/* *

- * C o p i e s a l l o f t h e m a p p i n g s f r o m t h e s p e c i ﬁ e d m a p t o t h i s m a p .

- * T h e s e m a p p i n g s w i l l r e p l a c e a n y m a p p i n g s t h a t t h i s m a p h a d f o r

- * a n y o f t h e k e y s c u r r e n t l y i n t h e s p e c i ﬁ e d m a p .

*

- * @ p a r a m m m a p p i n g s t o b e s t o r e d i n t h i s m a p

- * @ t h r o w s N u l l P o i n t e r E x c e p t i o n i f t h e s p e c i ﬁ e d m a p i s n u l l

- * /


public void putAll(Map<? extends K, ? extends V> m) {

putMapEntries(m, true); }

/* *

- * R e m o v e s t h e m a p p i n g f o r t h e s p e c i ﬁ e d k e y f r o m t h i s m a p i f p r e s e n t .

*

- * @ p a r a m k e y k e y w h o s e m a p p i n g i s t o b e r e m o v e d f r o m t h e m a p

- * @ r e t u r n t h e p r e v i o u s v a l u e a s s o c i a t e d w i t h < t t > k e y < /t t > , o r

- * < t t > n u l l < /t t > i f t h e r e w a s n o m a p p i n g f o r < t t > k e y < /t t > .

- * ( A < t t > n u l l < /t t > r e t u r n c a n a l s o i n d i c a t e t h a t t h e m ap

- * p r e v i o u s l y a s s o c i a t e d < t t > n u l l < /t t > w i t h < t t > k e y < /t t > . )

- * /


public V remove(Object key) { Node<K,V> e; return (e = removeNode(h a s h (key), key, null, false, true)) == null ?

null : e.value;

}

/* *

- * I m p l e m e n t s M a p . r e m o v e a n d r e l a t e d m e t h o d s

*

- * @ p a r a m h a s h h a s h f o r k e y

- * @ p a r a m k e y t h e k e y

- * @ p a r a m v a l u e t h e v a l u e t o m a t c h i f m a t c h V a l u e , e l s e i g n o r e d

- * @ p a r a m m a t c h V a l u e i f t r u e o n l y r e m o v e i f v a l u e i s e q u a l

- * @ p a r a m m o v a b l e i f f a l s e d o n o t m o v e o t h e r n o d e s w h i l e r e m o v i n g

- * @ r e t u r n t h e n o d e , o r n u l l i f n o n e

- * /


ﬁnal Node<K,V> removeNode(int hash, Object key, Object value,

boolean matchValue, boolean movable) { Node<K,V>[] tab; Node<K,V> p; int n, index; if ((tab = table) != null && (n = tab.length) > 0 &&

(p = tab[index = (n - 1) & hash]) != null) { Node<K,V> node = null, e; K k; V v; if (p.hash == hash &&

((k = p.key) == key || (key != null && key.equals(k)))) node = p;

else if ((e = p.next) != null) { if (p instanceof TreeNode)

node = ((TreeNode<K,V>)p).getTreeNode(hash, key); else {

do { if (e.hash == hash && ((k = e.key) == key ||

(key != null && key.equals(k)))) { node = e; break;

} p = e;

} while ((e = e.next) != null); }

} if (node != null && (!matchValue || (v = node.value) == value ||

(value != null && value.equals(v)))) { if (node instanceof TreeNode)

((TreeNode<K,V>)node).removeTreeNode(this, tab, movable); else if (node == p)

tab[index] = node.next; else

p.next = node.next;

++modCount;

--size; afterNodeRemoval(node); return node;

}

} return null;

}

/* *

- * R e m o v e s a l l o f t h e m a p p i n g s f r o m t h i s m a p .

- * T h e m a p w i l l b e e m p t y a f t e r t h i s c a l l r e t u r n s .

- * /


public void clear() { Node<K,V>[] tab; modCount++; if ((tab = table) != null && size > 0) {

size = 0; for (int i = 0; i < tab.length; ++i)

tab[i] = null; }

}

/* *

- * R e t u r n s < t t > t r u e < /t t > i f t h i s m a p m a p s o n e o r m o r e k e y s t o t h e

- * s p e c i ﬁ e d v a l u e .

*

- * @ p a r a m v a l u e v a l u e w h o s e p r e s e n c e i n t h i s m a p i s t o b e t e s t e d

- * @ r e t u r n < t t > t r u e < /t t > i f t h i s m a p m a p s o n e o r m o r e k e y s t o t h e

- * s p e c i ﬁ e d v a l u e


# * /

public boolean containsValue(Object value) { Node<K,V>[] tab; V v; if ((tab = table) != null && size > 0) {

for (int i = 0; i < tab.length; ++i) { for (Node<K,V> e = tab[i]; e != null; e = e.next) {

if ((v = e.value) == value || (value != null && value.equals(v))) return true;

} }

} return false;

}

/* *

- * R e t u r n s a { @ l i n k S e t } v i e w o f t h e k e y s c o n t a i n e d i n t h i s m a p .

- * T h e s e t i s b a c k e d b y t h e m a p , s o c h a n g e s t o t h e m a p a r e

- * r e ﬂ e c t e d i n t h e s e t , a n d v i c e - v e r s a . I f t h e m a p i s m o d i ﬁ e d

- * w h i l e a n i t e r a t i o n o v e r t h e s e t i s i n p r o g r e s s ( e x c e p t t h r o u g h

- * t h e i t e r a t o r ' s o w n < t t > r e m o v e < /t t > o p e r a t i o n ) , t h e r e s u l t s o f

- * t h e i t e r a t i o n a r e u n d e ﬁ n e d . T h e s e t s u p p o r t s e l e m e n t r e m o v a l ,

- * w h i c h r e m o v e s t h e c o r r e s p o n d i n g m a p p i n g f r o m t h e m a p , v i a t h e

- * < t t > I t e r a t o r . r e m o v e < /t t > , < t t > S e t . r e m o v e < /t t > ,

- * < t t > r e m o v e A l l < /t t > , < t t > r e t a i n A l l < /t t > , a n d < t t > c l e a r < /t t >

- * o p e r a t i o n s . I t d o e s n o t s u p p o r t t h e < t t > a d d < /t t > o r < t t > a d d A l l < /t t >

- * o p e r a t i o n s .

*

- * @ r e t u r n a s e t v i e w o f t h e k e y s c o n t a i n e d i n t h i s m a p

- * /


public Set<K> keySet() { Set<K> ks; return (ks = keySet) == null ? (keySet = new KeySet()) : ks;

}

ﬁnal class KeySet extends AbstractSet<K> { public ﬁnal int size() { return size; }

public ﬁnal void clear() { HashMap.this.clear(); } public ﬁnal Iterator<K> iterator() { return new KeyIterator(); } public ﬁnal boolean contains(Object o) { return containsKey(o); } public ﬁnal boolean remove(Object key) {

return removeNode(h a s h (key), key, null, false, true) != null;

} public ﬁnal Spliterator<K> spliterator() {

return new KeySpliterator<>(HashMap.this, 0, -1, 0, 0);

} public ﬁnal void forEach(Consumer<? super K> action) {

Node<K,V>[] tab; if (action == null)

throw new NullPointerException();

if (size > 0 && (tab = table) != null) { int mc = modCount; for (int i = 0; i < tab.length; ++i) {

for (Node<K,V> e = tab[i]; e != null; e = e.next) action.accept(e.key);

} if (modCount != mc)

throw new ConcurrentModiﬁcationException(); }

} }

/* *

- * R e t u r n s a { @ l i n k C o l l e c t i o n } v i e w o f t h e v a l u e s c o n t a i n e d i n t h i s m a p .

- * T h e c o l l e c t i o n i s b a c k e d b y t h e m a p , s o c h a n g e s t o t h e m a p a r e

- * r e ﬂ e c t e d i n t h e c o l l e c t i o n , a n d v i c e - v e r s a . I f t h e m a p i s

- * m o d i ﬁ e d w h i l e a n i t e r a t i o n o v e r t h e c o l l e c t i o n i s i n p r o g r e s s

- * ( e x c e p t t h r o u g h t h e i t e r a t o r ' s o w n < t t > r e m o v e < /t t > o p e r a t i o n ) ,

- * t h e r e s u l t s o f t h e i t e r a t i o n a r e u nd e ﬁ ne d . T h e c o l l e c t i o n

- * s u p p o r t s e l e m e n t r e m o v a l , w h i c h r e m o v e s t h e c o r r e s p o n d i n g

- * m a p p i n g f r o m t h e m a p , v i a t h e < t t > I t e r a t o r . r e m o v e < /t t > ,

- * < t t > C o l l e c t i o n . r e m o v e < /t t > , < t t > r e m o v e A l l < /t t > ,

- * < t t > r e t a i n A l l < /t t > a n d < t t > c l e a r < /t t > o p e r a t i o n s . I t d o e s n o t

- * s u p p o r t t h e < t t > a d d < /t t > o r < t t > a d d A l l < /t t > o p e r a t i o n s .


- *
- * @ r e t u r n a v i e w o f t h e v a l u e s c o n t a i n e d i n t h i s m a p

- * /


public Collection<V> values() { Collection<V> vs; return (vs = values) == null ? (values = new Values()) : vs;

}

ﬁnal class Values extends AbstractCollection<V> { public ﬁnal int size() { return size; } public ﬁnal void clear() { HashMap.this.clear(); } public ﬁnal Iterator<V> iterator() { return new ValueIterator(); } public ﬁnal boolean contains(Object o) { return containsValue(o); } public ﬁnal Spliterator<V> spliterator() {

return new ValueSpliterator<>(HashMap.this, 0, -1, 0, 0);

} public ﬁnal void forEach(Consumer<? super V> action) {

Node<K,V>[] tab; if (action == null)

throw new NullPointerException();

if (size > 0 && (tab = table) != null) { int mc = modCount; for (int i = 0; i < tab.length; ++i) {

for (Node<K,V> e = tab[i]; e != null; e = e.next) action.accept(e.value);

} if (modCount != mc)

throw new ConcurrentModiﬁcationException(); }

} }

/* *

- * R e t u r n s a { @ l i n k S e t } v i e w o f t h e m a p p i n g s c o n t a i n e d i n t h i s m a p .

- * T h e s e t i s b a c k e d b y t h e m a p , s o c h a n g e s t o t h e m a p a r e

- * r e ﬂ e c t e d i n t h e s e t , a n d v i c e - v e r s a . I f t h e m a p i s m o d i ﬁ e d

- * w h i l e a n i t e r a t i o n o v e r t h e s e t i s i n p r o g r e s s ( e x c e p t t h r o u g h


- * t h e i t e r a t o r ' s o w n < t t > r e m o v e < /t t > o p e r a t i o n , o r t h r o u g h t h e

- * < t t > s e t V a l u e < /t t > o p e r a t i o n o n a m a p e n t r y r e t u r n e d b y t h e

- * i t e r a t o r ) t h e r e s u l t s o f t h e i t e r at i o n a r e u nd e ﬁ ne d . T h e s e t

- * s u p p o r t s e l e m e n t r e m o v a l , w h i c h r e m o v e s t h e c o r r e s p o n d i n g

- * m a p p i n g f r o m t h e m a p , v i a t h e < t t > I t e r a t o r . r e m o v e < /t t > ,

- * < t t > S e t . r e m o v e < /t t > , < t t > r e m o v e A l l < /t t > , < t t > r e t a i n A l l < /t t > a n d

- * < t t > c l e a r < /t t > o p e r a t i o n s . I t d o e s n o t s u p p o r t t h e

- * < t t > a d d < /t t > o r < t t > a d d A l l < /t t > o p e r a t i o n s .

*

- * @ r e t u r n a s e t v i e w o f t h e m a p p i n g s c o n t a i n e d i n t h i s m a p

- * /


public Set<Map.Entry<K,V>> entrySet() { Set<Map.Entry<K,V>> es; return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;

}

ﬁnal class EntrySet extends AbstractSet<Map.Entry<K,V>> { public ﬁnal int size() { return size; } public ﬁnal void clear() { HashMap.this.clear(); } public ﬁnal Iterator<Map.Entry<K,V>> iterator() {

return new EntryIterator();

} public ﬁnal boolean contains(Object o) {

if (!(o instanceof Map.Entry))

return false; Map.Entry<?,?> e = (Map.Entry<?,?>) o; Object key = e.getKey(); Node<K,V> candidate = getNode(h a s h (key), key); return candidate != null && candidate.equals(e);

} public ﬁnal boolean remove(Object o) {

if (o instanceof Map.Entry) { Map.Entry<?,?> e = (Map.Entry<?,?>) o; Object key = e.getKey(); Object value = e.getValue(); return removeNode(h a s h (key), key, value, true, true) != null;

}

return false;

} public ﬁnal Spliterator<Map.Entry<K,V>> spliterator() {

return new EntrySpliterator<>(HashMap.this, 0, -1, 0, 0);

} public ﬁnal void forEach(Consumer<? super Map.Entry<K,V>> action) {

Node<K,V>[] tab; if (action == null)

throw new NullPointerException();

if (size > 0 && (tab = table) != null) { int mc = modCount; for (int i = 0; i < tab.length; ++i) {

for (Node<K,V> e = tab[i]; e != null; e = e.next) action.accept(e);

} if (modCount != mc)

throw new ConcurrentModiﬁcationException(); }

} }

# // O v e r r i d e s o f J D K 8 M a p e x t e n s i o n m e t h o d s

@Override public V getOrDefault(Object key, V defaultValue) {

Node<K,V> e; return (e = getNode(h a s h (key), key)) == null ? defaultValue : e.value;

}

@Override public V putIfAbsent(K key, V value) {

return putVal(h a s h (key), key, value, true, true); }

@Override public boolean remove(Object key, Object value) {

return removeNode(h a s h (key), key, value, true, true) != null;

}

@Override public boolean replace(K key, V oldValue, V newValue) {

Node<K,V> e; V v; if ((e = getNode(h a s h (key), key)) != null &&

((v = e.value) == oldValue || (v != null && v.equals(oldValue)))) { e.value = newValue; afterNodeAccess(e); return true;

} return false;

}

@Override public V replace(K key, V value) {

Node<K,V> e; if ((e = getNode(h a s h (key), key)) != null) {

V oldValue = e.value; e.value = value; afterNodeAccess(e); return oldValue;

} return null;

}

@Override public V computeIfAbsent(K key,

Function<? super K, ? extends V> mappingFunction) { if (mappingFunction == null)

break; }

++binCount; } while ((e = e.next) != null);

} V oldValue; if (old != null && (oldValue = old.value) != null) {

afterNodeAccess(old); return oldValue;

}

} V v = mappingFunction.apply(key); if (v == null) {

return null;

} else if (old != null) { old.value = v; afterNodeAccess(old); return v;

} else if (t != null)

t.putTreeVal(this, tab, hash, key, v);

else { tab[i] = newNode(hash, key, v, ﬁrst); if (binCount >= T R E E I F Y _T H R E S H O L D - 1)

treeifyBin(tab, hash); }

++modCount;

++size; afterNodeInsertion(true); return v;

}

public V computeIfPresent(K key,

BiFunction<? super K, ? super V, ? extends V> remappingFunction) { if (remappingFunction == null)

throw new NullPointerException(); Node<K,V> e; V oldValue; int hash = h a s h (key); if ((e = getNode(hash, key)) != null &&

(oldValue = e.value) != null) { V v = remappingFunction.apply(key, oldValue); if (v != null) {

e.value = v; afterNodeAccess(e); return v;

} else

removeNode(hash, key, null, false, true);

} return null;

}

@Override public V compute(K key,

BiFunction<? super K, ? super V, ? extends V> remappingFunction) { if (remappingFunction == null)

break; }

++binCount;

} while ((e = e.next) != null); }

} V oldValue = (old == null) ? null : old.value; V v = remappingFunction.apply(key, oldValue); if (old != null) {

if (v != null) { old.value = v; afterNodeAccess(old);

} else

removeNode(hash, key, null, false, true);

} else if (v != null) { if (t != null)

t.putTreeVal(this, tab, hash, key, v);

else { tab[i] = newNode(hash, key, v, ﬁrst); if (binCount >= T R E E I F Y _T H R E S H O L D - 1)

treeifyBin(tab, hash); }

++modCount;

++size;

afterNodeInsertion(true);

} return v;

}

@Override public V merge(K key, V value,

BiFunction<? super V, ? super V, ? extends V> remappingFunction) { if (value == null)

throw new NullPointerException(); if (remappingFunction == null)

throw new NullPointerException(); int hash = h a s h (key); Node<K,V>[] tab; Node<K,V> ﬁrst; int n, i; int binCount = 0; TreeNode<K,V> t = null; Node<K,V> old = null; if (size > threshold || (tab = table) == null ||

(n = tab.length) == 0) n = (tab = resize()).length;

if ((ﬁrst = tab[i = (n - 1) & hash]) != null) { if (ﬁrst instanceof TreeNode) old = (t = (TreeNode<K,V>)ﬁrst).getTreeNode(hash, key);

else { Node<K,V> e = ﬁrst; K k; do {

if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) { old = e; break;

}

++binCount;

} while ((e = e.next) != null); }

} if (old != null) {

V v;

if (old.value != null)

v = remappingFunction.apply(old.value, value); else

v = value;

if (v != null) { old.value = v; afterNodeAccess(old);

} else

removeNode(hash, key, null, false, true); return v;

} if (value != null) { if (t != null)

t.putTreeVal(this, tab, hash, key, value);

else { tab[i] = newNode(hash, key, value, ﬁrst); if (binCount >= T R E E I F Y _T H R E S H O L D - 1)

treeifyBin(tab, hash); }

++modCount;

++size; afterNodeInsertion(true);

} return value;

}

@Override public void forEach(BiConsumer<? super K, ? super V> action) {

Node<K,V>[] tab; if (action == null)

throw new NullPointerException();

if (size > 0 && (tab = table) != null) { int mc = modCount; for (int i = 0; i < tab.length; ++i) {

for (Node<K,V> e = tab[i]; e != null; e = e.next) action.accept(e.key, e.value);

} if (modCount != mc)

throw new ConcurrentModiﬁcationException(); }

}

@Override public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {

Node<K,V>[] tab; if (function == null)

throw new NullPointerException();

if (size > 0 && (tab = table) != null) { int mc = modCount; for (int i = 0; i < tab.length; ++i) {

for (Node<K,V> e = tab[i]; e != null; e = e.next) {

e.value = function.apply(e.key, e.value); }

} if (modCount != mc)

throw new ConcurrentModiﬁcationException(); }

}

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - * / // C l o n i n g a n d s e r i a l i z a t i o n

/* *

- * R e t u r n s a s h a l l o w c o p y o f t h i s < t t > H a s h M a p < /t t > i n s t a n c e : t h e k e y s a n d

- * v a l u e s t h e m s e l v e s a r e n o t c l o n e d .

*

- * @ r e t u r n a s h a l l o w c o p y o f t h i s m a p

- * /


@SuppressWarnings("unchecked") @Override public Object clone() {

HashMap<K,V> result; try {

result = (HashMap<K,V>)super.clone(); } catch (CloneNotSupportedException e) {

# // t h i s s h o u l d n ' t h a p p e n , s i n c e w e a r e C l o n e a b l e

throw new InternalError(e);

} result.reinitialize(); result.putMapEntries(this, false); return result;

}

# // T h e s e m e t h o d s a r e a l s o u s e d w h e n s e r i a l i z i n g H as h S e t s

ﬁnal ﬂoat loadFactor() { return loadFactor; } ﬁnal int capacity() {

return (table != null) ? table.length : (threshold > 0) ? threshold : D E F A U L T _IN I T I A L _C A P A C I T Y ;

}

/* *

- * S a v e t h e s t a t e o f t h e < t t > H a s h M a p < /t t > i n s t a n c e t o a s t r e a m ( i . e . ,

- * s e r i a l i z e i t ) .

*

- * @ s e r i a l D a t a T h e < i > c a p a c i t y < /i > o f t h e H a s h M a p ( t h e l e n g t h o f t h e

- * b u c k e t a r r a y ) i s e m i t t e d ( i n t ) , f o l l o w e d b y t h e

- * < i > s i z e < /i > ( a n i n t , t h e n u m b e r o f k e y - v a l u e

- * m a p p i n g s ) , f o l l o w e d b y t h e k e y ( O b j e c t ) a n d v a l u e ( O b j e c t )

- * f o r e a c h k e y - v a l u e m a p p i n g . T h e k e y - v a l u e m a p p i n g s a r e

- * e m i t t e d i n n o p a r t i c u l a r o r d e r .

- * /


private void writeObject(java.io.ObjectOutputStream s) throws IOException { int buckets = capacity();

# // W r i t e o u t t h e t h r e s h o l d , l o a d f a c t o r , a n d a n y h i d d e n s t u ﬀ

s.defaultWriteObject(); s.writeInt(buckets); s.writeInt(size); internalWriteEntries(s);

}

/* *

- * R e c o n s t i t u t e t h e { @ c o d e H a s h M a p } i n s t a n c e f r o m a s t r e a m ( i . e . ,

- * d e s e r i a l i z e i t ) .

- * /


private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {

// R e a d i n t h e t h r e s h o l d ( i g n o r e d ) , l o a d f ac t o r , a n d a n y h i d d e n s t u ﬀ s.defaultReadObject(); reinitialize(); if (loadFactor <= 0 || Float.i s N a N (loadFactor))

throw new InvalidObjectException("Illegal load factor: " +

loadFactor); s.readInt(); // R e a d a n d i g n o r e n u m b e r o f b u c k e t s int mappings = s.readInt(); // R e a d n u m b e r o f m a p p i n g s ( s i z e ) if (mappings < 0)

throw new InvalidObjectException("Illegal mappings count: " + mappings);

else if (mappings > 0) { // ( i f z e r o , u s e d e f a u l t s ) // S i z e t h e t a b l e u s i n g g i v e n l o a d f ac t o r o n l y i f w i t h i n // r a n g e o f 0 . 2 5 . . . 4 . 0 ﬂoat lf = Math.m i n (Math.m a x (0.25f, loadFactor), 4.0f); ﬂoat fc = (ﬂoat)mappings / lf + 1.0f; int cap = ((fc < D E F A U L T _IN I T I A L _C A P A C I T Y ) ?

# D E F A U L T _IN I T I A L _C A P A C I T Y : (fc >= M A X I M U M _C A P A C I T Y ) ? M A X I M U M _C A P A C I T Y : t a b l e S i z e F o r ((int)fc));

ﬂoat ft = (ﬂoat)cap * lf; threshold = ((cap < M A X I M U M _C A P A C I T Y && ft < M A X I M U M _C A P A C I T Y ) ?

(int)ft : Integer.M A X _V A L U E ); @SuppressWarnings({"rawtypes","unchecked"})

Node<K,V>[] tab = (Node<K,V>[])new Node[cap]; table = tab;

# // R e a d t h e k e y s a n d v a l u e s , a n d p u t t h e m a p p i n g s i n t h e H as h M ap

for (int i = 0; i < mappings; i++) { @SuppressWarnings("unchecked") K key = (K) s.readObject(); @SuppressWarnings("unchecked") V value = (V) s.readObject();

putVal(h a s h (key), key, value, false, false); }

} }

# /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - * / // i t e r a t o r s

abstract class HashIterator { Node<K,V> next; // n e x t e n t r y t o r e t u r n Node<K,V> current; // c u r r e n t e n t r y int expectedModCount; // f o r f a s t - f a i l int index; // c u r r e n t s l o t

HashIterator() { expectedModCount = modCount; Node<K,V>[] t = table; current = next = null; index = 0; if (t != null && size > 0) { // a d v a n c e t o ﬁ r s t e n t r y

do {} while (index < t.length && (next = t[index++]) == null); }

}

public ﬁnal boolean hasNext() {

return next != null; }

ﬁnal Node<K,V> nextNode() { Node<K,V>[] t; Node<K,V> e = next; if (modCount != expectedModCount)

throw new ConcurrentModiﬁcationException(); if (e == null)

throw new NoSuchElementException(); if ((next = (current = e).next) == null && (t = table) != null) { do {} while (index < t.length && (next = t[index++]) == null);

} return e;

}

public ﬁnal void remove() { Node<K,V> p = current; if (p == null)

throw new IllegalStateException(); if (modCount != expectedModCount) throw new ConcurrentModiﬁcationException();

current = null; K key = p.key; removeNode(h a s h (key), key, null, false, false);

expectedModCount = modCount; }

}

ﬁnal class KeyIterator extends HashIterator implements Iterator<K> { public ﬁnal K next() { return nextNode().key; }

}

ﬁnal class ValueIterator extends HashIterator implements Iterator<V> { public ﬁnal V next() { return nextNode().value; }

}

ﬁnal class EntryIterator extends HashIterator implements Iterator<Map.Entry<K,V>> { public ﬁnal Map.Entry<K,V> next() { return nextNode(); }

}

# /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - * / // s p l i t e r a t o r s

static class HashMapSpliterator<K,V> { ﬁnal HashMap<K,V> map; Node<K,V> current; // c u r r e n t n o d e int index; // c u r r e n t i n d e x , m o d i ﬁ e d o n a d v a nc e /s p l i t int fence; // o n e p a s t l a s t i n d e x int est; // s i z e e s t i m a t e int expectedModCount; // f o r c o m o d i ﬁ c a t i o n c h e c k s

HashMapSpliterator(HashMap<K,V> m, int origin, int fence, int est, int expectedModCount) {

this.map = m; this.index = origin; this.fence = fence; this.est = est; this.expectedModCount = expectedModCount;

}

ﬁnal int getFence() { // i n i t i a l i z e f e n c e a n d s i z e o n ﬁ r s t u s e int hi; if ((hi = fence) < 0) {

HashMap<K,V> m = map; est = m.size; expectedModCount = m.modCount; Node<K,V>[] tab = m.table; hi = fence = (tab == null) ? 0 : tab.length;

} return hi;

}

public ﬁnal long estimateSize() { getFence(); // f o r c e i n i t return (long) est;

}

}

static ﬁnal class KeySpliterator<K,V> extends HashMapSpliterator<K,V> implements Spliterator<K> { KeySpliterator(HashMap<K,V> m, int origin, int fence, int est,

int expectedModCount) {

super(m, origin, fence, est, expectedModCount); }

public KeySpliterator<K,V> trySplit() { int hi = getFence(), lo = index, mid = (lo + hi) >>> 1; return (lo >= mid || current != null) ? null :

new KeySpliterator<>(map, lo, index = mid, est >>>= 1, expectedModCount); }

public void forEachRemaining(Consumer<? super K> action) { int i, hi, mc; if (action == null)

throw new NullPointerException(); HashMap<K,V> m = map; Node<K,V>[] tab = m.table; if ((hi = fence) < 0) {

mc = expectedModCount = m.modCount; hi = fence = (tab == null) ? 0 : tab.length;

} else

mc = expectedModCount;

if (tab != null && tab.length >= hi && (i = index) >= 0 && (i < (index = hi) || current != null)) { Node<K,V> p = current; current = null; do {

if (p == null)

p = tab[i++]; else {

action.accept(p.key); p = p.next;

} } while (p != null || i < hi); if (m.modCount != mc)

throw new ConcurrentModiﬁcationException(); }

}

public boolean tryAdvance(Consumer<? super K> action) { int hi; if (action == null)

throw new NullPointerException(); Node<K,V>[] tab = map.table; if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {

while (current != null || index < hi) { if (current == null) current = tab[index++];

else { K k = current.key; current = current.next; action.accept(k); if (map.modCount != expectedModCount)

throw new ConcurrentModiﬁcationException(); return true;

} }

} return false;

}

public int characteristics() {

return (fence < 0 || est == map.size ? Spliterator.S I Z E D : 0) |

Spliterator.D I S T I N C T ; }

}

static ﬁnal class ValueSpliterator<K,V> extends HashMapSpliterator<K,V> implements Spliterator<V> { ValueSpliterator(HashMap<K,V> m, int origin, int fence, int est,

int expectedModCount) {

super(m, origin, fence, est, expectedModCount); }

public ValueSpliterator<K,V> trySplit() { int hi = getFence(), lo = index, mid = (lo + hi) >>> 1; return (lo >= mid || current != null) ? null :

new ValueSpliterator<>(map, lo, index = mid, est >>>= 1,

expectedModCount); }

public void forEachRemaining(Consumer<? super V> action) { int i, hi, mc; if (action == null)

throw new NullPointerException(); HashMap<K,V> m = map; Node<K,V>[] tab = m.table; if ((hi = fence) < 0) {

mc = expectedModCount = m.modCount; hi = fence = (tab == null) ? 0 : tab.length;

} else

mc = expectedModCount;

if (tab != null && tab.length >= hi && (i = index) >= 0 && (i < (index = hi) || current != null)) { Node<K,V> p = current; current = null; do {

if (p == null) p = tab[i++];

else { action.accept(p.value); p = p.next;

} } while (p != null || i < hi); if (m.modCount != mc)

throw new ConcurrentModiﬁcationException(); }

}

public boolean tryAdvance(Consumer<? super V> action) { int hi; if (action == null)

throw new NullPointerException(); Node<K,V>[] tab = map.table; if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {

while (current != null || index < hi) { if (current == null) current = tab[index++];

else { V v = current.value; current = current.next; action.accept(v); if (map.modCount != expectedModCount)

throw new ConcurrentModiﬁcationException(); return true;

} }

} return false;

}

public int characteristics() {

return (fence < 0 || est == map.size ? Spliterator.S I Z E D : 0); }

}

static ﬁnal class EntrySpliterator<K,V> extends HashMapSpliterator<K,V> implements Spliterator<Map.Entry<K,V>> {

EntrySpliterator(HashMap<K,V> m, int origin, int fence, int est, int expectedModCount) {

super(m, origin, fence, est, expectedModCount); }

public EntrySpliterator<K,V> trySplit() { int hi = getFence(), lo = index, mid = (lo + hi) >>> 1; return (lo >= mid || current != null) ? null :

new EntrySpliterator<>(map, lo, index = mid, est >>>= 1,

expectedModCount); }

public void forEachRemaining(Consumer<? super Map.Entry<K,V>> action) { int i, hi, mc; if (action == null)

throw new NullPointerException(); HashMap<K,V> m = map; Node<K,V>[] tab = m.table; if ((hi = fence) < 0) {

mc = expectedModCount = m.modCount; hi = fence = (tab == null) ? 0 : tab.length;

} else

mc = expectedModCount;

if (tab != null && tab.length >= hi && (i = index) >= 0 && (i < (index = hi) || current != null)) { Node<K,V> p = current; current = null; do {

if (p == null) p = tab[i++];

else { action.accept(p); p = p.next;

} } while (p != null || i < hi); if (m.modCount != mc)

throw new ConcurrentModiﬁcationException(); }

}

public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) { int hi; if (action == null)

throw new NullPointerException(); Node<K,V>[] tab = map.table; if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {

while (current != null || index < hi) { if (current == null) current = tab[index++];

else { Node<K,V> e = current; current = current.next; action.accept(e); if (map.modCount != expectedModCount)

throw new ConcurrentModiﬁcationException(); return true;

} }

} return false;

}

public int characteristics() {

return (fence < 0 || est == map.size ? Spliterator.S I Z E D : 0) |

Spliterator.D I S T I N C T ; }

}

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - * / // L i n k e d H a s h M a p s u p p o r t

/*

- * T h e f o l l o w i n g p a c k a g e - p r o t e c t e d m e t h o d s a r e d e s i g n e d t o b e

- * o v e r r i d d e n b y L i n k e d H a s h M ap , b u t n o t b y a n y o t h e r s u b c l a s s .

- * N e a r l y a l l o t h e r i n t e r n a l m e t h o d s a r e a l s o p a c k a g e - p r o t e c t e d

- * b u t a r e d e c l a r e d ﬁ n a l , s o c a n b e u s e d b y L i n k e d H as h M ap , v i e w

- * c l a s s e s , a n d H a s h S e t .

- * /


# // C r e a t e a r e g u l a r ( n o n - t r e e ) no d e

Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {

return new Node<>(hash, key, value, next); }

# // F o r c o n v e r s i o n f r o m T r e e N o d e s t o p l a i n n o d e s

Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next) {

return new Node<>(p.hash, p.key, p.value, next); }

# // C r e a t e a t r e e b i n n o d e

TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {

return new TreeNode<>(hash, key, value, next); }

# // F o r t r e e i f y B i n

TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {

return new TreeNode<>(p.hash, p.key, p.value, next); }

/* *

- * R e s e t t o i n i t i a l d e f a u l t s t a t e . C a l l e d b y c l o n e a n d r e a d O b j e c t .

- * /


void reinitialize() { table = null; entrySet = null; keySet = null; values = null; modCount = 0; threshold = 0;

size = 0; }

# // C a l l b a c k s t o a l l o w L i n k e d H a s h M a p p o s t - ac t i o n s

void afterNodeAccess(Node<K,V> p) { } void afterNodeInsertion(boolean evict) { } void afterNodeRemoval(Node<K,V> p) { }

# // C a l l e d o n l y f r o m w r i t e O b j e c t , t o e n s u r e c o m p a t i b l e o r d e r i n g .

void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException { Node<K,V>[] tab; if (size > 0 && (tab = table) != null) {

for (int i = 0; i < tab.length; ++i) {

for (Node<K,V> e = tab[i]; e != null; e = e.next) { s.writeObject(e.key); s.writeObject(e.value);

} }

} }

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - * / // T r e e b i n s

/* *

- * E n t r y f o r T r e e b i n s . E x t e n d s L i n k e d H as h M ap . E nt r y ( w h i c h i n t u r n

- * e x t e n d s N o d e ) s o c a n b e u s e d a s e x t e n s i o n o f e i t h e r r e g u l a r o r

- * l i n k e d n o d e .

- * /


static ﬁnal class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> { TreeNode<K,V> parent; // r e d - b l a c k t r e e l i n k s TreeNode<K,V> left; TreeNode<K,V> right; TreeNode<K,V> prev; // n e e d e d t o u n l i n k n e x t u p o n d e l e t i o n boolean red; TreeNode(int hash, K key, V val, Node<K,V> next) {

super(hash, key, val, next);

}

/* *

- * R e t u r n s r o o t o f t r e e c o n t a i n i n g t h i s n o d e .

- * /


ﬁnal TreeNode<K,V> root() { for (TreeNode<K,V> r = this, p;;) { if ((p = r.parent) == null)

return r; r = p;

} }

/* *

- * E n s u r e s t h a t t h e g i v e n r o o t i s t h e ﬁ r s t n o d e o f i t s b i n .

- * /


static <K,V> void moveRootToFront(Node<K,V>[] tab, TreeNode<K,V> root) { int n; if (root != null && tab != null && (n = tab.length) > 0) {

int index = (n - 1) & root.hash; TreeNode<K,V> ﬁrst = (TreeNode<K,V>)tab[index]; if (root != ﬁrst) {

Node<K,V> rn; tab[index] = root; TreeNode<K,V> rp = root.prev; if ((rn = root.next) != null)

((TreeNode<K,V>)rn).prev = rp; if (rp != null)

rp.next = rn; if (ﬁrst != null)

ﬁrst.prev = root;

root.next = ﬁrst; root.prev = null;

} assert c h e c k I n v a r i a n t s (root);

} }

/* *

- * F i n d s t h e n o d e s t a r t i n g a t r o o t p w i t h t h e g i v e n h a s h a n d k e y .

- * T h e k c a r g u m e n t c a c h e s c o m p a r ab l e C l a s s F o r ( k e y ) u p o n ﬁ r s t u s e

- * c o m p a r i n g k e y s .

- * /


ﬁnal TreeNode<K,V> ﬁnd(int h, Object k, Class<?> kc) { TreeNode<K,V> p = this; do {

int ph, dir; K pk; TreeNode<K,V> pl = p.left, pr = p.right, q; if ((ph = p.hash) > h)

p = pl; else if (ph < h) p = pr; else if ((pk = p.key) == k || (k != null && k.equals(pk))) return p; else if (pl == null) p = pr; else if (pr == null) p = pl; else if ((kc != null ||

(kc = c o m p a r a b l e C l a s s F o r (k)) != null) &&

(dir = c o m p a r e C o m p a r a b l e s (kc, k, pk)) != 0) p = (dir < 0) ? pl : pr;

else if ((q = pr.ﬁnd(h, k, kc)) != null)

return q; else

p = pl; } while (p != null); return null;

}

/* *

- * C a l l s ﬁ n d f o r r o o t n o d e .

- * /


ﬁnal TreeNode<K,V> getTreeNode(int h, Object k) {

return ((parent != null) ? root() : this).ﬁnd(h, k, null); }

/* *

- * T i e - b r e a k i n g u t i l i t y f o r o r d e r i n g i n s e r t i o n s w h e n e q u al

- * h a s h C o d e s a n d n o n - c o m p a r ab l e . W e d o n ' t r e q u i r e a t o t a l

- * o r d e r , j u s t a c o n s i s t e n t i n s e r t i o n r u l e t o m a i n t a i n

- * e q u i v a l e n c e a c r o s s r e b a l a n c i n g s . T i e - b r e a k i n g f u r t h e r t h a n

- * n e c e s s a r y s i m p l i ﬁ e s t e s t i n g a b i t .

- * /


static int tieBreakOrder(Object a, Object b) { int d; if (a == null || b == null ||

(d = a.getClass().getName(). compareTo(b.getClass().getName())) == 0)

d = (System.i d e n t i t y H a s h C o d e (a) <= System.i d e n t i t y H a s h C o d e (b) ?

-1 : 1); return d;

}

/* *

- * F o r m s t r e e o f t h e n o d e s l i n k e d f r o m t h i s n o d e .

- * @ r e t u r n r o o t o f t r e e

- * /


ﬁnal void treeify(Node<K,V>[] tab) { TreeNode<K,V> root = null; for (TreeNode<K,V> x = this, next; x != null; x = next) {

next = (TreeNode<K,V>)x.next; x.left = x.right = null; if (root == null) {

x.parent = null; x.red = false; root = x;

} else {

K k = x.key; int h = x.hash;

Class<?> kc = null; for (TreeNode<K,V> p = root;;) {

int dir, ph; K pk = p.key; if ((ph = p.hash) > h)

dir = -1; else if (ph < h) dir = 1; else if ((kc == null &&

(kc = c o m p a r a b l e C l a s s F o r (k)) == null) ||

(dir = c o m p a r e C o m p a r a b l e s (kc, k, pk)) == 0) dir = t i e B r e a k O r d e r (k, pk);

TreeNode<K,V> xp = p; if ((p = (dir <= 0) ? p.left : p.right) == null) {

x.parent = xp; if (dir <= 0)

xp.left = x; else

xp.right = x; root = b a l a n c e I n s e r t i o n (root, x); break;

} }

}

} m o v e R o o t T o F r o n t (tab, root);

}

/* *

- * R e t u r n s a l i s t o f n o n - T r e e N o d e s r e p l a c i n g t h o s e l i n k e d f r o m

- * t h i s n o d e .

- * /


ﬁnal Node<K,V> untreeify(HashMap<K,V> map) { Node<K,V> hd = null, tl = null; for (Node<K,V> q = this; q != null; q = q.next) {

Node<K,V> p = map.replacementNode(q, null);

if (tl == null) hd = p; else

tl.next = p; tl = p;

} return hd;

}

/* *

- * T r e e v e r s i o n o f p u t V a l .

- * /


ﬁnal TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab, int h, K k, V v) {

Class<?> kc = null; boolean searched = false; TreeNode<K,V> root = (parent != null) ? root() : this; for (TreeNode<K,V> p = root;;) {

int dir, ph; K pk; if ((ph = p.hash) > h)

dir = -1; else if (ph < h) dir = 1; else if ((pk = p.key) == k || (k != null && k.equals(pk))) return p; else if ((kc == null &&

(kc = c o m p a r a b l e C l a s s F o r (k)) == null) || (dir = c o m p a r e C o m p a r a b l e s (kc, k, pk)) == 0) {

if (!searched) { TreeNode<K,V> q, ch; searched = true; if (((ch = p.left) != null &&

(q = ch.ﬁnd(h, k, kc)) != null) || ((ch = p.right) != null &&

(q = ch.ﬁnd(h, k, kc)) != null)) return q;

dir = t i e B r e a k O r d e r (k, pk); }

TreeNode<K,V> xp = p; if ((p = (dir <= 0) ? p.left : p.right) == null) {

Node<K,V> xpn = xp.next; TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn); if (dir <= 0)

xp.left = x; else

xp.right = x; xp.next = x; x.parent = x.prev = xp; if (xpn != null)

((TreeNode<K,V>)xpn).prev = x; m o v e R o o t T o F r o n t (tab, b a l a n c e I n s e r t i o n (root, x)); return null;

} }

}

/* *

- * R e m o v e s t h e g i v e n n o d e , t h a t m u s t b e p r e s e n t b e f o r e t h i s c a l l .

- * T h i s i s m e s s i e r t h a n t y p i c a l r e d - b l a c k d e l e t i o n c o d e b e c a u s e w e

- * c a n n o t s w a p t h e c o n t e n t s o f a n i n t e r i o r n o d e w i t h a l e a f

- * s u c c e s s o r t h a t i s p i n n e d b y " n e x t " p o i n t e r s t h a t a r e a c c e s s i b l e

- * i n d e p e n d e n t l y d u r i n g t r a v e r s a l . S o i n s t e a d w e s w a p t h e t r e e

- * l i n k a g e s . I f t h e c u r r e n t t r e e a p p e a r s t o h a v e t o o f e w n o d e s ,

- * t h e b i n i s c o n v e r t e d b a c k t o a p l a i n b i n . ( T h e t e s t t r i g g e r s

- * s o m e w h e r e b e t w e e n 2 a n d 6 n o d e s , d e p e n d i n g o n t r e e s t r u c t u r e ) .

- * /


ﬁnal void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab, boolean movable) {

int n; if (tab == null || (n = tab.length) == 0)

return; int index = (n - 1) & hash;

TreeNode<K,V> ﬁrst = (TreeNode<K,V>)tab[index], root = ﬁrst, rl; TreeNode<K,V> succ = (TreeNode<K,V>)next, pred = prev; if (pred == null)

tab[index] = ﬁrst = succ; else

pred.next = succ; if (succ != null)

succ.prev = pred; if (ﬁrst == null)

return; if (root.parent != null) root = root.root();

if (root == null || root.right == null || (rl = root.left) == null || rl.left == null) { tab[index] = ﬁrst.untreeify(map); // t o o s m a l l return;

} TreeNode<K,V> p = this, pl = left, pr = right, replacement; if (pl != null && pr != null) {

TreeNode<K,V> s = pr, sl; while ((sl = s.left) != null) // ﬁ n d s u c c e s s o r

s = sl; boolean c = s.red; s.red = p.red; p.red = c; // s w a p c o l o r s TreeNode<K,V> sr = s.right; TreeNode<K,V> pp = p.parent; if (s == pr) { // p w a s s ' s d i r e c t p a r e n t

p.parent = s; s.right = p;

} else {

TreeNode<K,V> sp = s.parent; if ((p.parent = sp) != null) {

if (s == sp.left)

sp.left = p; else

sp.right = p;

if ((s.right = pr) != null) pr.parent = s;

} p.left = null; if ((p.right = sr) != null)

sr.parent = p; if ((s.left = pl) != null) pl.parent = s; if ((s.parent = pp) == null) root = s; else if (p == pp.left)

pp.left = s; else

pp.right = s; if (sr != null)

replacement = sr; else

replacement = p;

} else if (pl != null)

replacement = pl; else if (pr != null)

replacement = pr; else

replacement = p; if (replacement != p) { TreeNode<K,V> pp = replacement.parent = p.parent; if (pp == null)

root = replacement; else if (p == pp.left)

pp.left = replacement; else

pp.right = replacement;

p.left = p.right = p.parent = null; }

TreeNode<K,V> r = p.red ? root : b a l a n c e D e l e t i o n (root, replacement);

if (replacement == p) { // d e t a c h TreeNode<K,V> pp = p.parent; p.parent = null; if (pp != null) {

if (p == pp.left) pp.left = null; else if (p == pp.right)

pp.right = null; }

} if (movable)

# m o v e R o o t T o F r o n t (tab, r); }

/* *

- * S p l i t s n o d e s i n a t r e e b i n i n t o l o w e r a n d u p p e r t r e e b i n s ,

- * o r u n t r e e i ﬁ e s i f n o w t o o s m a l l . C a l l e d o n l y f r o m r e s i z e ;

- * s e e a b o v e d i s c u s s i o n a b o u t s p l i t b i t s a n d i n d i c e s .

*

- * @ p a r a m m a p t h e m a p

- * @ p a r a m t a b t h e t a b l e f o r r e c o r d i n g b i n h e a d s

- * @ p a r a m i n d e x t h e i n d e x o f t h e t a b l e b e i n g s p l i t

- * @ p a r a m b i t t h e b i t o f h a s h t o s p l i t o n

- * /


ﬁnal void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit) { TreeNode<K,V> b = this;

# // R e l i n k i n t o l o a n d h i l i s t s , p r e s e r v i n g o r d e r

TreeNode<K,V> loHead = null, loTail = null; TreeNode<K,V> hiHead = null, hiTail = null; int lc = 0, hc = 0; for (TreeNode<K,V> e = b, next; e != null; e = next) {

next = (TreeNode<K,V>)e.next; e.next = null; if ((e.hash & bit) == 0) {

if ((e.prev = loTail) == null) loHead = e;

else

loTail.next = e; loTail = e;

++lc;

} else {

if ((e.prev = hiTail) == null)

hiHead = e; else

hiTail.next = e; hiTail = e;

++hc; }

}

if (loHead != null) {

# if (lc <= U N T R E E I F Y _T H R E S H O L D )

tab[index] = loHead.untreeify(map);

else { tab[index] = loHead; if (hiHead != null) // ( e l s e i s a l r e a d y t r e e i ﬁ e d )

loHead.treeify(tab); }

} if (hiHead != null) {

# if (hc <= U N T R E E I F Y _T H R E S H O L D )

tab[index + bit] = hiHead.untreeify(map);

else { tab[index + bit] = hiHead; if (loHead != null)

hiHead.treeify(tab); }

} }

# /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - * / // R e d - b l a c k t r e e m e t h o d s , a l l a d a p t e d f r o m C L R

static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root, TreeNode<K,V> p) {

TreeNode<K,V> r, pp, rl; if (p != null && (r = p.right) != null) { if ((rl = p.right = r.left) != null)

rl.parent = p; if ((pp = r.parent = p.parent) == null)

(root = r).red = false; else if (pp.left == p)

pp.left = r; else

pp.right = r; r.left = p; p.parent = r;

} return root;

}

static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root, TreeNode<K,V> p) {

TreeNode<K,V> l, pp, lr; if (p != null && (l = p.left) != null) { if ((lr = p.left = l.right) != null)

lr.parent = p; if ((pp = l.parent = p.parent) == null)

(root = l).red = false; else if (pp.right == p)

pp.right = l; else

pp.left = l; l.right = p; p.parent = l;

} return root;

}

static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root, TreeNode<K,V> x) {

x.red = true; for (TreeNode<K,V> xp, xpp, xppl, xppr;;) {

if ((xp = x.parent) == null) { x.red = false; return x;

} else if (!xp.red || (xpp = xp.parent) == null)

return root; if (xp == (xppl = xpp.left)) {

if ((xppr = xpp.right) != null && xppr.red) { xppr.red = false; xp.red = false; xpp.red = true; x = xpp;

} else {

if (x == xp.right) { root = r o t a t e L e f t (root, x = xp); xpp = (xp = x.parent) == null ? null : xp.parent;

} if (xp != null) {

xp.red = false; if (xpp != null) {

xpp.red = true; root = r o t a t e R i g h t (root, xpp);

} }

}

} else {

if (xppl != null && xppl.red) { xppl.red = false; xp.red = false; xpp.red = true; x = xpp;

else {

if (x == xp.left) { root = r o t a t e R i g h t (root, x = xp); xpp = (xp = x.parent) == null ? null : xp.parent;

} if (xp != null) {

xp.red = false; if (xpp != null) {

xpp.red = true; root = r o t a t e L e f t (root, xpp);

} }

} }

} }

static <K,V> TreeNode<K,V> balanceDeletion(TreeNode<K,V> root,

TreeNode<K,V> x) { for (TreeNode<K,V> xp, xpl, xpr;;) {

if (x == null || x == root) return root;

else if ((xp = x.parent) == null) { x.red = false; return x;

} else if (x.red) {

x.red = false; return root;

} else if ((xpl = xp.left) == x) {

if ((xpr = xp.right) != null && xpr.red) { xpr.red = false; xp.red = true; root = r o t a t e L e f t (root, xp); xpr = (xp = x.parent) == null ? null : xp.right;

if (xpr == null) x = xp;

else { TreeNode<K,V> sl = xpr.left, sr = xpr.right; if ((sr == null || !sr.red) && (sl == null || !sl.red)) { xpr.red = true; x = xp;

} else {

if (sr == null || !sr.red) { if (sl != null)

sl.red = false; xpr.red = true; root = r o t a t e R i g h t (root, xpr); xpr = (xp = x.parent) == null ?

null : xp.right;

} if (xpr != null) {

xpr.red = (xp == null) ? false : xp.red; if ((sr = xpr.right) != null)

sr.red = false;

} if (xp != null) {

xp.red = false; root = r o t a t e L e f t (root, xp);

} x = root;

} }

# } else { // s y m m e t r i c

if (xpl != null && xpl.red) { xpl.red = false; xp.red = true; root = r o t a t e R i g h t (root, xp);

xpl = (xp = x.parent) == null ? null : xp.left;

} if (xpl == null)

x = xp;

else { TreeNode<K,V> sl = xpl.left, sr = xpl.right; if ((sl == null || !sl.red) &&

(sr == null || !sr.red)) { xpl.red = true; x = xp;

} else {

if (sl == null || !sl.red) { if (sr != null)

sr.red = false; xpl.red = true; root = r o t a t e L e f t (root, xpl); xpl = (xp = x.parent) == null ?

null : xp.left;

} if (xpl != null) {

xpl.red = (xp == null) ? false : xp.red; if ((sl = xpl.left) != null)

sl.red = false;

} if (xp != null) {

xp.red = false; root = r o t a t e R i g h t (root, xp);

} x = root;

} }

} }

}

# /* *

- * R e c u r s i v e i n v a r i a n t c h e c k

- * /


static <K,V> boolean checkInvariants(TreeNode<K,V> t) { TreeNode<K,V> tp = t.parent, tl = t.left, tr = t.right,

tb = t.prev, tn = (TreeNode<K,V>)t.next; if (tb != null && tb.next != t)

return false; if (tn != null && tn.prev != t) return false; if (tp != null && t != tp.left && t != tp.right) return false; if (tl != null && (tl.parent != t || tl.hash > t.hash)) return false; if (tr != null && (tr.parent != t || tr.hash < t.hash)) return false; if (t.red && tl != null && tl.red && tr != null && tr.red) return false;

if (tl != null && !c h e c k I n v a r i a n t s (tl))

return false;

if (tr != null && !c h e c k I n v a r i a n t s (tr))

return false; return true;

} }

}

