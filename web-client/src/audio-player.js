class AudioPlayer {
  constructor(){
    this.audio = null;
  }
  start(url){
    if(!this.audio){
      this.audio = new Audio(url);
    }
    else{
      this.audio.setAttribute('src',url);
      this.audio.load();
    }
    this.audio.play();
  }
}

let instance;

if(!instance){
  instance = new AudioPlayer()
}

export default instance
