Generate SVG/PNG plots of hierarchical state charts defined in a YAML
file.

The YAML format is based on the format defined by
[Sismic](https://github.com/AlexandreDecan/sismic). The examples under
`sismic` come from that project and are copyrighted
by their authors, under the original license (LGPLv3).

The main change w.r.t. the Sismic format is that it's possible to
define multiple actions in a transition. E.g. for single actions you
can use:

	action: myAction

but if you need multiple actions, you can use:

	actions:
	  - myAction1
	  - myAction2
	  # ...

You cannot use both `action` and `actions`.
