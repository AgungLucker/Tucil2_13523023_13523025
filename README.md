
# Tugas Kecil 2 IF2211 Strategi Algoritma
> Kompresi Gambar Dengan Metode Quadtree 

Quadtree adalah struktur data hierarkis yang digunakan untuk membagi ruang atau data dua dimensi ke dalam empat kuadran secara rekursif. Struktur ini sangat berguna dalam berbagai bidang, salah satunya adalah pengolahan citra. Dalam konteks kompresi gambar, Quadtree digunakan untuk menyederhanakan blok-blok gambar berdasarkan keseragaman nilai warnanya. QuadTree dipilih sebagai metode kompresi gambar karena mampu mengurangi ukuran file secara signifikan tanpa mengorbankan detail penting pada gambar.

Program ini mengimplementasikan kompresi gambar menggunakan algoritma Divide and Conquer melalui metode Quadtree. Prosesnya dimulai dengan membagi gambar menjadi blok-blok secara rekursif, berdasarkan perhitungan error dari tiap blok menggunakan metode evaluasi yang dipilih. Blok akan terus dibagi menjadi empat sub-blok jika ukuran dan error-nya belum memenuhi kriteria minimal. Ketika sebuah blok telah cukup kecil atau sudah cukup seragam, maka proses pembagian dihentikan, dan blok tersebut dianggap sebagai leaf node pada Quadtree.


## Made by
Muhammad Aufa Farabi - 13523023
Joel Hotlan Haris Siahaan - 13523023

## Features
* Kompresi gambar dengan Quadtree
* Pilihan Metode pengukuran error dalam kompresi
* Hasil kompresi disimpan dalam file gambar (.jpg) dan GIF


## How to Run
1. Clone repository pada terminal
   ```sh
   git clone https://github.com/AgungLucker/Tucil2_13523023_13523025
   ```
2. Pindah ke direktori src untuk memulai program
    ```sh
    cd src
    ```
2. Compile program 
    ```sh
    javac *.java
    ```
3. Jalankan program dengan command berikut
    ```sh
    java Main.java 
    ```

## Links
- Project Homepage:
(https://github.com/AgungLucker/Tucil2_13523023_13523025)
- GIF Library Reference:
(https://github.com/rtyley/animated-gif-lib-for-java)

