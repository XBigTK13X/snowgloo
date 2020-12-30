var assert = require('assert')
var settings = require('./settings')

const MusicFile = require('./music-file')

const parseFile = relativePath => {
    return new MusicFile(settings.mediaRoot + '/' + relativePath)
}

describe('MusicFile', function() {
    const SmashBrosSong = parseFile('Game/Smash Bros/Vol. 35 - Fatal Fury (2018)/001 - Haremar Faith Capoeira School - Song of the Fight (Believers Will Be Saved) - FATAL FURY - 8ec7c9cbb5c038a66f7802a3b9f6f220.adjusted.mp3"')
    describe('Anime', function() {
        const AnimeSong = parseFile('Anime/A Place Further Than the Universe/OP IN ED (2018)/001 - The Girls Are Alright! - 1ec7c9cbb5c038a66f7802a3b9f6f220.adjusted.mp3')
        it('should parse metadata', function() {
            assert.equal(AnimeSong.Title, 'The Girls Are Alright!')
            assert.equal(AnimeSong.DisplayAlbum, 'OP IN ED')
            assert.equal(AnimeSong.DisplayArtist, 'A Place Further Than the Universe')
        })
    })
    describe('Artist', function() {
        it('should preserve title dashes', function() {
            assert.equal(SmashBrosSong.Title, 'Haremar Faith Capoeira School - Song of the Fight (Believers Will Be Saved)')
        })
        const MultiDiscSong = parseFile('Game/Nintendo Switch/The Legend of Zelda Breath of the Wild (2018)/D03T31 - Urbosa and the Divine Beast - 7ec7c9cbb5c038a66f7802a3b9f6f220.adjusted.mp3')
        it('should read track count from multidisc albums', function() {
            assert.equal(MultiDiscSong.Disc, 3)
            assert.equal(MultiDiscSong.Track, 31)
        })
        const ArtistSong = parseFile('Artist/Barenaked Ladies/Gordon (1992)/04 - Brian Wilson - 2ec7c9cbb5c038a66f7802a3b9f6f220.adjusted.mp3')
        it('should parse metadata', function() {
            assert.equal(ArtistSong.Title, 'Brian Wilson')
            assert.equal(ArtistSong.DisplayAlbum, 'Gordon')
            assert.equal(ArtistSong.DisplayArtist, 'Barenaked Ladies')
        })
    })
    describe('Compilation', function() {
        const CompilationSong = parseFile('Compilation/Anime Hits (2019)/001 - Tori Kago (ED1) - Darling in the Franxx - 3ec7c9cbb5c038a66f7802a3b9f6f220.adjusted.mp3')
        console.log({ CompilationSong })
        it('should parse metadata', function() {
            assert.equal(CompilationSong.Title, 'Tori Kago (ED1)')
            assert.equal(CompilationSong.DisplayAlbum, 'Anime Hits')
            assert.equal(CompilationSong.DisplayArtist, 'Darling in the Franxx')
        })
    })
    describe('Disney', function() {
        const DisneySong = parseFile('Disney/Brave (2012)/001 - Touch The Sky - 4ec7c9cbb5c038a66f7802a3b9f6f220.adjusted.mp3')
        it('should parse metadata', function() {
            assert.equal(DisneySong.Title, 'Touch The Sky')
            assert.equal(DisneySong.DisplayAlbum, 'Brave')
            assert.equal(DisneySong.DisplayArtist, 'Disney')
        })
    })
    describe('Game', function() {
        const GameSong = new MusicFile('media/trove/media/music/Game/Wii U/Rayman Legends (2013)/007 - Score Recap - 5ec7c9cbb5c038a66f7802a3b9f6f220.adjusted.mp3')
        it('should parse metadata', function() {
            assert.equal(GameSong.Title, 'Score Recap')
            assert.equal(GameSong.DisplayAlbum, 'Rayman Legends')
            assert.equal(GameSong.DisplayArtist, 'Wii U')
        })
        const GameSongIdCollisionFirst = parseFile('Game/SNES/Super Mario All-Stars (1993)/D02T001 - Title - Super Mario Bros. 2 - 9ec7c9cbb5c038a66f7802a3b9f6f222.adjusted.mp3')
        const GameSongIdCollisionSecond = parseFile('Game/SNES/Super Mario All-Stars (1993)/D01T001 - Title - Super Mario Bros. - 1ac7c9cbb5c038a66f7802a3b9f6f221.adjusted.mp3')
        it('should use extra file info to prevent song ID collision', function() {
            assert.notEqual(GameSongIdCollisionFirst.Id, GameSongIdCollisionSecond.Id)
        })
    })
    describe('Movie', function() {
        const MovieSong = parseFile('Movie/La La Land (2016)/001 - Another Day of Sun - 6ec7c9cbb5c038a66f7802a3b9f6f220.adjusted.mp3')
        it('should parse metadata', function() {
            assert.equal(MovieSong.Title, 'Another Day of Sun')
            assert.equal(MovieSong.DisplayAlbum, 'La La Land')
            assert.equal(MovieSong.DisplayArtist, 'Movie')
        })
    })
    describe('Smash Bros', function() {
        it('should parse metadata', function() {
            assert.equal(SmashBrosSong.Title, 'Haremar Faith Capoeira School - Song of the Fight (Believers Will Be Saved)')
            assert.equal(SmashBrosSong.DisplayAlbum, 'Fatal Fury')
            assert.equal(SmashBrosSong.DisplayArtist, 'FATAL FURY')
        })
        it('should remove .Vol from album title', function() {
            assert.equal(SmashBrosSong.DisplayAlbum, 'Fatal Fury')
        })
    })
    describe('Messy File Names', function() {
        it('should safely encode percent sign', function() {
            const FirstMessySong = parseFile('Anime/K-On/Image Songs - Main Cast Part 1 (2013.06)/008 - Mokujise Happy 100% - 709b6c5b53e298b4c0e92fe2b613d5cf.adjusted.mp3')
            let hasPercent = FirstMessySong.AudioUrl.indexOf('100% ')
            assert.equal(hasPercent, -1)
        })
        it('should safely encode a hash character', function() {
            const SecondMessySong = parseFile('Anime/K-On/K-ON Commerical Breaks (2013.14)/025 - Subtitle Track #25 - 5b8f446128ea1b0e32b5fa6f37e03b88.adjusted.mp3')
            let hasHash = SecondMessySong.AudioUrl.indexOf('#')
            assert.equal(hasHash, -1)
        })
    })
})
