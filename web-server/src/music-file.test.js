var assert = require('assert')

const MusicFile = require('./music-file')

const AnimeSong = new MusicFile('/media/trove/media/music/Anime/A Place Further Than the Universe/OP IN ED (2018)/001 - The Girls Are Alright!.mp3')
const ArtistSong = new MusicFile('/media/trove/media/music/Artist/Barenaked Ladies/Gordon (1992)/04 - Brian Wilson.mp3')
const CompilationSong = new MusicFile('/media/trove/media/music/Compilation/Anime Hits (2019)/001 - Tori Kago (ED1) - Darling in the Franxx.mp3')
const DisneySong = new MusicFile('/media/trove/media/music/Disney/Brave (2012)/001 - Touch The Sky.mp3')
const GameSong  = new MusicFile('media/trove/media/music/Game/Wii U/Rayman Legends (2013)/007 - Score Recap.mp3')
const MovieSong = new MusicFile('/media/trove/media/music/Movie/La La Land (2016)/001 - Another Day of Sun.mp3')
const MultiDiscSong = new MusicFile('/media/trove/media/music/Game/Nintendo Switch/The Legend of Zelda Breath of the Wild (2018)/D03T31 - Urbosa and the Divine Beast.mp3')
const SmashBrosSong = new MusicFile('/media/trove/media/music/Game/Smash Bros/Vol. 35 - Fatal Fury (2018)/001 - Haremar Faith Capoeira School - Song of the Fight (Believers Will Be Saved) - FATAL FURY.mp3"')


describe('MusicFile', function() {
    describe('Anime', function(){
        it('should parse metadata', function() {
            assert.equal(AnimeSong.Title, 'The Girls Are Alright!')
            assert.equal(AnimeSong.DisplayAlbum,"OP IN ED")
            assert.equal(AnimeSong.DisplayArtist,"A Place Further Than the Universe")
        })
    })
    describe('Artist', function(){
        it('should preserve title dashes', function() {
            assert.equal(SmashBrosSong.Title, 'Haremar Faith Capoeira School - Song of the Fight (Believers Will Be Saved) - FATAL FURY')
        })
        it('should read track count from multidisc albums', function(){
            assert.equal(MultiDiscSong.Disc, 3)
            assert.equal(MultiDiscSong.Track, 31)
        })
        it('should parse metadata', function(){
            assert.equal(ArtistSong.Title, 'Brian Wilson')
            assert.equal(ArtistSong.DisplayAlbum,"Gordon")
            assert.equal(ArtistSong.DisplayArtist,"Barenaked Ladies")
        })
    })
    describe('Compilation', function(){
        it('should parse metadata', function() {
            assert.equal(CompilationSong.Title, 'Tori Kago (ED1)')
            assert.equal(CompilationSong.DisplayAlbum,"Anime Hits")
            assert.equal(CompilationSong.DisplayArtist,"Darling in the Franxx")
        })
    })
    describe('Disney', function(){
        it('should parse metadata', function() {
            assert.equal(DisneySong.Title, 'Touch The Sky')
            assert.equal(DisneySong.DisplayAlbum,"Brave")
            assert.equal(DisneySong.DisplayArtist,"Disney")
        })
    })
    describe('Game', function(){
        it('should parse metadata', function(){
            assert.equal(GameSong.Title, 'Score Recap')
            assert.equal(GameSong.DisplayAlbum,"Rayman Legends")
            assert.equal(GameSong.DisplayArtist,"Wii U")

        })
    })
    describe('Movie', function(){
        it('should parse metadata', function(){
            assert.equal(MovieSong.Title, 'Another Day of Sun')
            assert.equal(MovieSong.DisplayAlbum,"La La Land")
            assert.equal(MovieSong.DisplayArtist,"Movie")
        })
    })
    describe('Smash Bros', function() {
        it('should parse metadata', function(){
            assert.equal(SmashBrosSong.Title, 'Haremar Faith Capoeira School - Song of the Fight (Believers Will Be Saved) - FATAL FURY')
            assert.equal(SmashBrosSong.DisplayAlbum,"Fatal Fury")
            assert.equal(SmashBrosSong.DisplayArtist,"Smash Bros")
        })
        it('should remove .Vol from album title', function() {
            assert.equal(SmashBrosSong.DisplayAlbum, 'Fatal Fury')
        })
    })
})
