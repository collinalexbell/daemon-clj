#!/bin/bash
script_dir=$(dirname $0)
unbuffer pocketsphinx_continuous -inmic yes -keyphrase "ok dag knee" -kws_threshold "\1e-10" -logfn /dev/null > $script_dir/pipe 
