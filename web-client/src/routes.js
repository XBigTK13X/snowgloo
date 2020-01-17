import pages from './page'

export default [
    {
        name: 'home',
        url: '/',
        component: pages.Home,
    },
    {
        name: 'artist-list',
        url: '/artist/list',
        component: pages.ArtistList,
    },
    {
        name: 'album-list',
        url: '/album/list',
        component: pages.AlbumList,
    },
    {
        name: 'artist-view',
        url: '/artist/view/:artist',
        component: pages.ArtistView,
    },
    {
        name: 'album-view',
        url: '/album/view/:albumSlug',
        component: pages.AlbumView,
    },
    {
        name: 'song-list',
        url: '/song/list',
        component: pages.SongList,
    },
]
