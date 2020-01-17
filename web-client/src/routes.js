import pages from './page';

export default [
    {
      name: 'home',
      url: '/',
      component: pages.Home,
    },
    {
      name: 'artist-list',
      url: '/artist/list',
      component: pages.ArtistList
    },
    {
      name: 'album-list',
      url: '/album/list',
      component: pages.AlbumList
    },
    {
      name: 'song-list',
      url: '/song/list',
      component: pages.SongList
    }
]
