#!/usr/bin/env python3
import sys
import os
import urllib.parse as urlparser
import pathlib


def main():
    if len(sys.argv) <= 1:
        raise RuntimeError("Must provide redirect URL!")

    url = sys.argv[1]
    url_parsed = urlparser.urlparse(url)
    result = urlparser.parse_qs(url_parsed.query)

    code = result['code'][0]
    state = result['state'][0]


    root_dir = pathlib.Path(state)

    os.makedirs(root_dir, exist_ok=True)
    with open(root_dir / '.oauthcode', 'w') as f:
        f.write(code)

    success_file = root_dir / '.done'
    success_file.touch()


if __name__ == '__main__':
    main()
