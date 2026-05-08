"""
yt-dlp wrapper called from Kotlin via Chaquopy.

Uses single-file formats only, so ffmpeg is NOT required to be bundled.
This caps YouTube quality at ~720p (the highest single-format mp4 stream).
To unlock higher quality, bundle ffmpeg-kit and remove the format restriction.
"""
import os
import yt_dlp


def download(url: str, output_dir: str) -> str:
    """Download a video to output_dir. Returns the resulting file path."""
    os.makedirs(output_dir, exist_ok=True)

    ydl_opts = {
        # Prefer mp4 single-file streams. Falls back to best available single file.
        # 'best' (without merging) avoids needing ffmpeg.
        'format': 'best[ext=mp4]/best[ext=webm]/best',
        'outtmpl': os.path.join(output_dir, '%(title).80s [%(id)s].%(ext)s'),
        'noplaylist': True,
        'quiet': True,
        'no_warnings': True,
        'restrictfilenames': True,
        # Don't try to use external ffmpeg
        'prefer_ffmpeg': False,
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        info = ydl.extract_info(url, download=True)
        if info is None:
            raise RuntimeError("Could not extract video info")
        # If a playlist sneaks in, take the first entry
        if 'entries' in info:
            info = info['entries'][0]
        filename = ydl.prepare_filename(info)
        return filename


def get_title(url: str) -> str:
    """Get just the video title (used for preview before downloading)."""
    ydl_opts = {
        'quiet': True,
        'no_warnings': True,
        'noplaylist': True,
        'skip_download': True,
    }
    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        info = ydl.extract_info(url, download=False)
        if info is None:
            return "Unknown"
        if 'entries' in info:
            info = info['entries'][0]
        return info.get('title', 'Unknown')
