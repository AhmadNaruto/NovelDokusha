# NovelDokusha
Android web novel reader. Reader focused on simplicity, improving read immersion.
Search from a large catalog of content, open your pick and just enjoy.

# License
Copyright © 2023, [nani](https://github.com/nanihadesuka), Released under [GPL-3](LICENSE) FOSS

## Features
  - Two databases to search for web novels (by title or categories)
  - Multiple sources from where to read
  - Reader
    - Infinite scroll
    - Custom font
    - Custom font size
    - Live translation to more than 40 languages
    - Text to speech with:
      - Champter play controls
      - Custom voice selection
        - Voice, pitch, speed
        - Option to save prefered combinations
  - Light and dark themes
  - Epubs importer (format won't be preserved)
  - Easy backup and restore (zip file of a database + images folder)
  - Currenlty has 6 english sources (default) and 1 brazilian.
  
## Screenshots
 
Library | Finder
:-------------------------:|:-------------------------:
![](screenshots/library.png)  |  ![](screenshots/finder.png)
Book info | Book chapters
![](screenshots/book_info.png)  |  ![](screenshots/book_chapers.png)
Reader | 
![](screenshots/reader.png)  |   


## Tech stack
  - Kotlin
  - XML views
  - Jetpack compose
  - Coroutines
  - LiveData
  - Room (SQLite)
  - Jsoup
  - Coil  
  - Gson
  - Google MLKit for translation
  - Android TTS